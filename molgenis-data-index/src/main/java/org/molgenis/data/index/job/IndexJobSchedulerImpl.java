package org.molgenis.data.index.job;

import static com.google.common.collect.Streams.stream;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.index.meta.IndexActionMetadata.END_DATE_TIME;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_STATUS;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.PENDING;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus.STARTED;
import static org.molgenis.data.index.meta.IndexActionMetadata.TRANSACTION_ID;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.apache.commons.collections.list.SynchronizedList;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
import org.molgenis.data.index.queue.RunnableIndexAction;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class IndexJobSchedulerImpl implements IndexJobScheduler {

  private final ExecutorService executorService;
  private final IndexJobService indexJobService;
  private final DataService dataService;

  private final Lock lock = new ReentrantLock();
  private final Condition allEntitiesStable = lock.newCondition();
  private final Condition singleEntityStable = lock.newCondition();
  private final List<RunnableIndexAction> indexActions =
      SynchronizedList.decorate(new ArrayList<RunnableIndexAction>());

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexJobSchedulerImpl.class);

  public IndexJobSchedulerImpl(
      IndexJobService indexJobService, ExecutorService executorService, DataService dataService) {
    this.dataService = requireNonNull(dataService);
    this.indexJobService = requireNonNull(indexJobService);
    this.executorService = requireNonNull(executorService);
  }

  @Override
  @RunAsSystem
  public void scheduleIndexActions(String transactionId) {
    LOGGER.trace("Index transaction with id {}...", transactionId);
    var query = new QueryImpl<IndexAction>().eq(TRANSACTION_ID, transactionId);
    dataService.findAll(INDEX_ACTION, query, IndexAction.class).forEach(this::schedule);
  }

  @Override
  @RunAsSystem
  public void schedule(IndexAction indexAction) {
    var task = new RunnableIndexAction(indexAction, indexJobService);

    if (workExists(indexAction, task)) {
      return;
    }

    indexActions.stream()
        .filter(other -> other.isContainedBy(task))
        .filter(other -> other.getStatus() == PENDING)
        .forEach(
            other -> {
              LOGGER.info(
                  "Canceled pending index action {} contained by the work of index action {}!",
                  other,
                  indexAction);
              other.setStatus(IndexActionMetadata.IndexStatus.CANCELED);
            });

    addTask(task);
    CompletableFuture.runAsync(task, executorService).whenComplete((a, b) -> removeTask(task));
  }

  private boolean workExists(IndexAction indexAction, RunnableIndexAction task) {
    AtomicBoolean exists = new AtomicBoolean(false);
    indexActions.stream()
        .filter(other -> other.contains(task))
        .findAny()
        .ifPresent(
            other -> {
              LOGGER.info(
                  "Skipping index action {} because the work is already contained in action {}",
                  indexAction,
                  other);
              task.setStatus(IndexStatus.CANCELED);
              exists.set(true);
            });
    return exists.get();
  }

  void addTask(RunnableIndexAction task) {
    lock.lock();
    try {
      indexActions.add(task);
    } finally {
      lock.unlock();
    }
  }

  void removeTask(RunnableIndexAction task) {
    lock.lock();
    try {
      indexActions.remove(task);
      if (indexActions.isEmpty()) {
        allEntitiesStable.signalAll();
      }
      singleEntityStable.signalAll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void waitForAllIndicesStable() throws InterruptedException {
    lock.lock();
    try {
      while (!isAllIndicesStable()) {
        allEntitiesStable.await();
      }
    } finally {
      lock.unlock();
    }
  }

  private boolean isIndexStableIncludingReferences(EntityType emd) {
    if (isAllIndicesStable()) {
      return true;
    }
    Set<String> referencedEntityIds =
        stream(emd.getAtomicAttributes())
            .filter(Attribute::hasRefEntity)
            .map(attribute -> attribute.getRefEntity().getId())
            .collect(toSet());
    referencedEntityIds.add(emd.getId());
    return referencedEntityIds.stream()
        .noneMatch(
            entityTypeId ->
                indexActions.stream().anyMatch(action -> action.concerns(entityTypeId)));
  }

  public void waitForIndexToBeStableIncludingReferences(EntityType emd)
      throws InterruptedException {
    lock.lock();
    try {
      while (!isIndexStableIncludingReferences(emd)) {
        singleEntityStable.await();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Cleans up IndexActions that finished longer than five minutes ago. Delay for a minute to allow
   * the transaction manager to become available
   */
  @Scheduled(initialDelay = 60 * 1000, fixedRate = 5 * 60 * 1000)
  @Override
  public void cleanupIndexActions() {
    runAsSystem(
        () -> {
          LOGGER.trace("Clean up IndexActions...");
          Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
          if (dataService.hasRepository(INDEX_ACTION)) {
            Stream<Entity> actions =
                dataService
                    .getRepository(IndexActionMetadata.INDEX_ACTION)
                    .query()
                    .nest()
                    .lt(END_DATE_TIME, fiveMinutesAgo)
                    .or()
                    .eq(END_DATE_TIME, null)
                    .unnest()
                    .and()
                    .not()
                    .in(INDEX_STATUS, asList(STARTED, PENDING))
                    .findAll();
            dataService.delete(INDEX_ACTION, actions);
            LOGGER.debug("Cleaned up IndexActions");
          } else {
            LOGGER.warn("{} does not exist", INDEX_ACTION);
          }
        });
  }

  private boolean isAllIndicesStable() {
    return indexActions.isEmpty();
  }
}
