package org.molgenis.data.cache.l2;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;

import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.transaction.TransactionInformation;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Adds, removes and retrieves entities from the {@link L2Cache} when a {@link Repository} is {@link
 * RepositoryCapability#CACHEABLE}.
 *
 * <p>Delegates to the underlying repository when an action is not supported by the cache or when
 * the cache doesn't contain the needed entity.
 */
public class L2CacheRepositoryDecorator extends AbstractRepositoryDecorator<Entity> {
  private static final int ID_BATCH_SIZE = 1000;

  private final L2Cache l2Cache;

  private final boolean cacheable;

  private final TransactionInformation transactionInformation;

  public L2CacheRepositoryDecorator(
      Repository<Entity> delegateRepository,
      L2Cache l2Cache,
      TransactionInformation transactionInformation) {
    super(delegateRepository);
    this.l2Cache = requireNonNull(l2Cache);
    this.cacheable = delegateRepository.getCapabilities().containsAll(newArrayList(CACHEABLE));
    this.transactionInformation = transactionInformation;
  }

  /**
   * Retrieves a single entity by id.
   *
   * @param id the entity's ID value
   * @return the retrieved Entity, or null if not present.
   */
  @Override
  public Entity findOneById(Object id) {
    if (doRetrieveFromCache(id)) {
      return l2Cache.get(delegate(), id);
    }
    return delegate().findOneById(id);
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public Stream<Entity> findAll(Stream<Object> ids) {
    if (doRetrieveFromCache()) {
      Iterator<List<Object>> batches = Iterators.partition(ids.iterator(), ID_BATCH_SIZE);
      Iterable<List<Object>> iterable = () -> batches;
      return Streams.stream(iterable).flatMap(batch -> findAllCache(batch).stream());
    } else {
      return delegate().findAll(ids);
    }
  }

  @SuppressWarnings("UnstableApiUsage")
  @Override
  public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch) {
    if (doRetrieveFromCache()) {
      Iterator<List<Object>> batches = Iterators.partition(ids.iterator(), ID_BATCH_SIZE);
      Iterable<List<Object>> iterable = () -> batches;
      return Streams.stream(iterable).flatMap(batch -> findAllCache(batch, fetch).stream());
    } else {
      return delegate().findAll(ids, fetch);
    }
  }

  @Override
  public Entity findOneById(Object id, Fetch fetch) {
    if (doRetrieveFromCache(id)) {
      return l2Cache.get(delegate(), id, fetch);
    }
    return delegate().findOneById(id, fetch);
  }

  private boolean doRetrieveFromCache() {
    return cacheable
        && (TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            || !transactionInformation.isEntireRepositoryDirty(getEntityType()));
  }

  private boolean doRetrieveFromCache(Object id) {
    return cacheable
        && (TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            || (!transactionInformation.isEntireRepositoryDirty(getEntityType())
                && !transactionInformation.isEntityDirty(EntityKey.create(getEntityType(), id))));
  }

  /**
   * Retrieves a batch of Entity IDs.
   *
   * <p>If currently in transaction, splits the ids into those that have been dirtied in the current
   * transaction and those that have been left untouched. The untouched ids are loaded through the
   * cache, the dirtied ids are loaded from the decorated repository directly.
   *
   * @param ids list of entity IDs to retrieve
   * @return List of {@link Entity}s, missing ones excluded.
   */
  private List<Entity> findAllCache(List<Object> ids) {
    if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
      return l2Cache.getBatch(delegate(), ids);
    } else {
      String entityTypeId = getEntityType().getId();
      Multimap<Boolean, Object> partitionedIds =
          Multimaps.index(
              ids, id -> transactionInformation.isEntityDirty(EntityKey.create(entityTypeId, id)));
      Collection<Object> cleanIds = partitionedIds.get(false);
      Collection<Object> dirtyIds = partitionedIds.get(true);

      Map<Object, Entity> result =
          newHashMap(uniqueIndex(l2Cache.getBatch(delegate(), cleanIds), Entity::getIdValue));
      result.putAll(
          delegate().findAll(dirtyIds.stream()).collect(toMap(Entity::getIdValue, e -> e)));

      return ids.stream().filter(result::containsKey).map(result::get).collect(toList());
    }
  }

  private List<Entity> findAllCache(List<Object> ids, Fetch fetch) {
    if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
      return l2Cache.getBatch(delegate(), ids, fetch);
    } else {
      String entityTypeId = getEntityType().getId();
      Multimap<Boolean, Object> partitionedIds =
          Multimaps.index(
              ids, id -> transactionInformation.isEntityDirty(EntityKey.create(entityTypeId, id)));
      Collection<Object> cleanIds = partitionedIds.get(false);
      Collection<Object> dirtyIds = partitionedIds.get(true);

      List<Entity> batch = l2Cache.getBatch(delegate(), cleanIds, fetch);
      Map<Object, Entity> result = newHashMap(uniqueIndex(batch, Entity::getIdValue));
      result.putAll(
          delegate().findAll(dirtyIds.stream(), fetch).collect(toMap(Entity::getIdValue, e -> e)));

      return ids.stream().filter(result::containsKey).map(result::get).collect(toList());
    }
  }
}
