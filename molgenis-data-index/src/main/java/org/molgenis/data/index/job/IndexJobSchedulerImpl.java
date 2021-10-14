package org.molgenis.data.index.job;

import static com.google.common.collect.Streams.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.index.meta.IndexActionGroupMetadata.INDEX_ACTION_GROUP;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION_GROUP_ATTR;
import static org.molgenis.jobs.model.JobExecution.Status.SUCCESS;
import static org.molgenis.jobs.model.JobExecutionMetaData.END_DATE;
import static org.molgenis.jobs.model.JobExecutionMetaData.STATUS;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.index.meta.IndexAction;
import org.molgenis.data.index.meta.IndexActionGroup;
import org.molgenis.data.index.meta.IndexActionMetadata;
import org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;
import org.molgenis.data.index.queue.RunnableIndexAction;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
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
  private final List<RunnableIndexAction> indexActions = new CopyOnWriteArrayList<>();

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexJobSchedulerImpl.class);

  public IndexJobSchedulerImpl(
      IndexJobService indexJobService, ExecutorService jobExecutor, DataService dataService) {
    this.dataService = requireNonNull(dataService);
    this.indexJobService = requireNonNull(indexJobService);
    this.executorService = requireNonNull(jobExecutor);
  }

  @Override
  public void scheduleIndexJob(String transactionId) {
    LOGGER.trace("Index transaction with id {}...", transactionId);
    IndexActionGroup indexActionGroup =
        dataService.findOneById(INDEX_ACTION_GROUP, transactionId, IndexActionGroup.class);
    if (indexActionGroup != null) {
      var query = new QueryImpl<IndexAction>().eq(INDEX_ACTION_GROUP_ATTR, indexActionGroup);
      dataService.findAll(INDEX_ACTION, query, IndexAction.class).forEach(this::schedule);
    }
  }

  @Override
  public void schedule(IndexAction indexAction) {
    var task = new RunnableIndexAction(indexAction, indexJobService);
    if (indexActions.stream().anyMatch(other -> other.contains(task))) {
      LOGGER.info(
          "An action containing the work of index action {} is already scheduled, skipping!",
          indexAction);
      task.setStatus(IndexStatus.CANCELED);
      return;
    }
    indexActions.stream()
        .filter(other -> other.isContainedBy(task))
        .filter(other -> other.getStatus() == IndexActionMetadata.IndexStatus.PENDING)
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
   * Cleans up successful IndexJobExecutions that finished longer than five minutes ago. delay for a
   * minute to allow the transaction manager to become available
   */
  @Scheduled(initialDelay = 1 * 60 * 1000, fixedRate = 5 * 60 * 1000)
  @Override
  public void cleanupJobExecutions() {
    runAsSystem(
        () -> {
          LOGGER.trace("Clean up Index job executions...");
          Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);
          boolean indexJobExecutionExists =
              dataService.hasRepository(IndexJobExecutionMetadata.INDEX_JOB_EXECUTION);
          if (indexJobExecutionExists) {
            Stream<Entity> executions =
                dataService
                    .getRepository(IndexJobExecutionMetadata.INDEX_JOB_EXECUTION)
                    .query()
                    .lt(END_DATE, fiveMinutesAgo)
                    .and()
                    .eq(STATUS, SUCCESS.toString())
                    .findAll();
            dataService.delete(IndexJobExecutionMetadata.INDEX_JOB_EXECUTION, executions);
            LOGGER.debug("Cleaned up Index job executions.");
          } else {
            LOGGER.warn("{} does not exist", IndexJobExecutionMetadata.INDEX_JOB_EXECUTION);
          }
        });
  }

  private boolean isAllIndicesStable() {
    return indexActions.isEmpty();
  }
}
