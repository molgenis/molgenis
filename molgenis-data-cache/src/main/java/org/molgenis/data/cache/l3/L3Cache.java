package org.molgenis.data.cache.l3;

import static com.google.common.collect.Maps.newConcurrentMap;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/** In-memory Query cache containing Queries and resulting ids from cacheable repositories */
@Service
public class L3Cache implements TransactionListener {
  private static final Logger LOG = getLogger(L3Cache.class);
  private static final int MAX_CACHE_SIZE_PER_QUERY = 1000;

  /** maps entity name to the loading cache with Query key and List of Identifiers */
  private final ConcurrentMap<String, LoadingCache<Query<Entity>, List<Object>>> caches =
      newConcurrentMap();

  private final TransactionInformation transactionInformation;

  private final MeterRegistry meterRegistry;

  public L3Cache(
      TransactionManager transactionManager,
      TransactionInformation transactionInformation,
      MeterRegistry meterRegistry) {
    this.transactionInformation = requireNonNull(transactionInformation);
    this.meterRegistry = requireNonNull(meterRegistry);
    requireNonNull(transactionManager).addTransactionListener(this);
  }

  @Override
  public void afterCommitTransaction(String transactionId) {
    transactionInformation.getDirtyRepositories().forEach(caches::remove);
  }

  public List<Object> get(Repository<Entity> repository, Query<Entity> query) {
    // Set fetch to null because we are only caching identifiers
    LoadingCache<Query<Entity>, List<Object>> cache = getQueryCache(repository);
    Query<Entity> fetchlessQuery = new QueryImpl<>(query);
    fetchlessQuery.setFetch(null);
    return cache.getUnchecked(fetchlessQuery);
  }

  @SuppressWarnings("squid:S2201") // ignore return values
  private LoadingCache<Query<Entity>, List<Object>> getQueryCache(Repository<Entity> repository) {
    String id = repository.getEntityType().getId();
    if (!caches.containsKey(id)) {
      caches.putIfAbsent(id, createQueryCache(repository));
    }
    return caches.get(id);
  }

  private LoadingCache<Query<Entity>, List<Object>> createQueryCache(
      Repository<Entity> repository) {
    LOG.trace("Creating Query cache for repository {}", repository.getName());
    LoadingCache<Query<Entity>, List<Object>> cache =
        CaffeinatedGuava.build(
            Caffeine.newBuilder()
                .recordStats()
                .maximumSize(MAX_CACHE_SIZE_PER_QUERY)
                .expireAfterAccess(10, MINUTES),
            createCacheLoader(repository));
    GuavaCacheMetrics.monitor(meterRegistry, cache, "l3." + repository.getEntityType().getId());
    return cache;
  }

  /**
   * Create a cacheloader that loads entity ids from the repository and stores them together with
   * their query
   *
   * @return the {@link CacheLoader}
   */
  private CacheLoader<Query<Entity>, List<Object>> createCacheLoader(
      final Repository<Entity> repository) {
    String repositoryName = repository.getName();
    Fetch idAttributeFetch =
        new Fetch().field(repository.getEntityType().getIdAttribute().getName());
    return new CacheLoader<Query<Entity>, List<Object>>() {
      /**
       * Loads {@link Entity} identifiers for a {@link Query}
       *
       * @param query the cache key to load
       * @return {@link List} of identifier {@link Object}s
       */
      @Override
      public List<Object> load(@Nonnull Query<Entity> query) {
        LOG.trace("Loading identifiers from repository {} for query {}", repositoryName, query);
        return repository
            .findAll(new QueryImpl<>(query).fetch(idAttributeFetch))
            .map(Entity::getIdValue)
            .collect(toList());
      }
    };
  }

  /** Logs cumulative cache statistics for all known caches. */
  @Scheduled(fixedRate = 60000)
  public void logStatistics() {
    // TODO: do we want to log diff with last log instead?
    if (LOG.isDebugEnabled()) {
      LOG.debug("Cache stats:");
      for (Map.Entry<String, LoadingCache<Query<Entity>, List<Object>>> cacheEntry :
          caches.entrySet()) {
        LOG.debug("{}:{}", cacheEntry.getKey(), cacheEntry.getValue().stats());
      }
    }
  }
}
