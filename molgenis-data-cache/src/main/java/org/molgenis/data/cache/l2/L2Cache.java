package org.molgenis.data.cache.l2;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.TransactionInformation;
import org.molgenis.data.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Maps.newConcurrentMap;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

/**
 * In-memory cache of entities read from cacheable repositories.
 */
@Service
public class L2Cache extends DefaultMolgenisTransactionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(L2Cache.class);
	private static final int MAX_CACHE_SIZE_PER_ENTITY = 1000;
	/**
	 * maps entity id to the loading cache with Object key and Optional dehydrated entity value
	 */
	private final ConcurrentMap<String, LoadingCache<Object, Optional<Map<String, Object>>>> caches;
	private final EntityHydration entityHydration;
	private final TransactionInformation transactionInformation;

	public L2Cache(TransactionManager transactionManager, EntityHydration entityHydration,
			TransactionInformation transactionInformation)
	{
		this.entityHydration = requireNonNull(entityHydration);
		this.transactionInformation = requireNonNull(transactionInformation);
		caches = newConcurrentMap();
		requireNonNull(transactionManager).addTransactionListener(this);
	}

	@Override
	public void afterCommitTransaction(String transactionId)
	{
		//TODO: trace logging
		transactionInformation.getEntirelyDirtyRepositories().forEach(caches::remove);
		transactionInformation.getDirtyEntities().forEach(this::evict);
	}

	private void evict(EntityKey entityKey)
	{
		LoadingCache<Object, Optional<Map<String, Object>>> cache = caches.get(entityKey.getEntityTypeId());
		if (cache != null)
		{
			cache.invalidate(entityKey.getId());
		}
	}

	/**
	 * Retrieves an entity from the cache or the underlying repository.
	 *
	 * @param repository the underlying repository
	 * @param id         the ID of the entity to retrieve
	 * @return the retrieved Entity, or null if the entity is not present.
	 * @throws com.google.common.util.concurrent.UncheckedExecutionException if the repository throws an error when
	 *                                                                       loading the entity
	 */
	public Entity get(Repository<Entity> repository, Object id)
	{
		LoadingCache<Object, Optional<Map<String, Object>>> cache = getEntityCache(repository);
		EntityType entityType = repository.getEntityType();
		return cache.getUnchecked(id).map(e -> entityHydration.hydrate(e, entityType)).orElse(null);
	}

	/**
	 * Retrieves a list of entities from the cache. If the cache doesn't yet exist, will create the cache.
	 *
	 * @param repository the underlying repository, used to create the cache loader or to retrieve the existing cache
	 * @param ids        {@link Iterable} of the ids of the entities to retrieve
	 * @return List containing the retrieved entities, missing values are excluded
	 * @throws RuntimeException if the cache failed to load the entities
	 */
	public List<Entity> getBatch(Repository<Entity> repository, Iterable<Object> ids)
	{
		try
		{
			return getEntityCache(repository).getAll(ids)
											 .values()
											 .stream()
											 .filter(Optional::isPresent)
											 .map(Optional::get)
											 .map(e -> entityHydration.hydrate(e, repository.getEntityType()))
											 .collect(Collectors.toList());
		}
		catch (ExecutionException exception)
		{
			// rethrow unchecked
			if (exception.getCause() != null && exception.getCause() instanceof RuntimeException)
			{
				throw (RuntimeException) exception.getCause();
			}
			throw new MolgenisDataException(exception);
		}
	}

	/**
	 * Logs cumulative cache statistics for all known caches.
	 */
	@Scheduled(fixedRate = 60000)
	public void logStatistics()
	{
		//TODO: do we want to log diff with last log instead?
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Cache stats:");
			for (Map.Entry<String, LoadingCache<Object, Optional<Map<String, Object>>>> cacheEntry : caches.entrySet())
			{
				LOG.debug("{}:{}", cacheEntry.getKey(), cacheEntry.getValue().stats());
			}
		}
	}

	/**
	 * Gets the existing entity cache for a {@link Repository} or creates a new one if no cache exists yet.
	 *
	 * @param repository the Repository used to create a new cache if none found, otherwise only the id of the
	 *                   repository is used to look up the existing cache
	 * @return the LoadingCache for the repository
	 */
	private LoadingCache<Object, Optional<Map<String, Object>>> getEntityCache(Repository<Entity> repository)
	{
		String id = repository.getEntityType().getId();
		if (!caches.containsKey(id))
		{
			caches.putIfAbsent(id, createEntityCache(repository));
		}
		return caches.get(id);
	}

	/**
	 * Creates a new Entity cache
	 *
	 * @param repository the {@link Repository} to load the entities from
	 * @return newly created LoadingCache
	 */
	private LoadingCache<Object, Optional<Map<String, Object>>> createEntityCache(Repository<Entity> repository)
	{
		Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder().recordStats().expireAfterAccess(10, MINUTES);
		if (!MetaDataService.isMetaEntityType(repository.getEntityType()))
		{
			cacheBuilder.maximumSize(MAX_CACHE_SIZE_PER_ENTITY);
		}
		return CaffeinatedGuava.build(cacheBuilder, createCacheLoader(repository));
	}

	/**
	 * Creates a CacheLoader that loads entities from the repository and dehydrates them.
	 *
	 * @param repository the Repository to load the entities from
	 * @return the {@link CacheLoader}
	 */
	private CacheLoader<Object, Optional<Map<String, Object>>> createCacheLoader(final Repository<Entity> repository)
	{
		return new CacheLoader<Object, Optional<Map<String, Object>>>()
		{
			/**
			 * Loads a single entity from the repository.
			 * @param id ID value of the entity to retrieve
			 * @return dehydrated entity or empty if the entity was not present in the repository
			 */
			@Override
			public Optional<Map<String, Object>> load(@Nonnull Object id)
			{
				return Optional.ofNullable(repository.findOneById(id)).map(entityHydration::dehydrate);
			}

			/**
			 * Loads multiple entities from the repository.
			 * @param ids Iterable of String representations of the ID values
			 * @return Map mapping id to loaded entity, or to empty optional if the entity was not present in the repository
			 */
			@Override
			public Map<Object, Optional<Map<String, Object>>> loadAll(Iterable<?> ids)
			{
				Stream<Object> typedIds = stream(ids.spliterator(), false).map(id -> id);
				Map<Object, Optional<Map<String, Object>>> result = repository.findAll(typedIds)
																			  .collect(toMap(Entity::getIdValue,
																					  this::dehydrateEntity));
				for (Object key : ids)
				{
					// cache the absence of these entities in the backend as empty values
					result.putIfAbsent(key, empty());
				}
				return result;
			}

			private Optional<Map<String, Object>> dehydrateEntity(Entity entity)
			{
				return Optional.of(entityHydration.dehydrate(entity));
			}
		};
	}
}
