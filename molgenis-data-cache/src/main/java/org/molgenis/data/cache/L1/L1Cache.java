package org.molgenis.data.cache.l1;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.molgenis.data.Entity;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.cache.utils.CombinedEntityCache;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Caches entities within a transaction to speed up queries within those transactions. Each transaction has its own
 * cache. When the transaction is committed the cache is removed.
 */
@Component
public class L1Cache extends DefaultMolgenisTransactionListener
{
	private static final Logger LOG = getLogger(L1Cache.class);
	private static final int MAX_CACHE_SIZE = 1000;
	private final ThreadLocal<CombinedEntityCache> caches;
	private final EntityHydration entityHydration;

	@Autowired
	public L1Cache(MolgenisTransactionManager molgenisTransactionManager, EntityHydration entityHydration)
	{
		caches = new ThreadLocal<>();
		this.entityHydration = requireNonNull(entityHydration);
		requireNonNull(molgenisTransactionManager).addTransactionListener(this);
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		LOG.trace("Creating L1 cache for transaction [{}]", transactionId);
		caches.set(createCache());
	}

	private CombinedEntityCache createCache()
	{
		Cache<String, Optional<Map<String, Object>>> cache = CacheBuilder.newBuilder().maximumSize(MAX_CACHE_SIZE)
				.build();
		return new CombinedEntityCache(entityHydration, cache);
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			LOG.trace("Cleaning up L1 cache after transaction [{}]", transactionId);
			caches.remove();
		}
	}

	/**
	 * Evicts an entity from the cache.
	 *
	 * @param entityName name of the entity to evict
	 * @param entityId   id value of the entity to evict
	 */
	public void evict(String entityName, Object entityId)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			LOG.trace("Removing  entity [{}] from L1 cache that belongs to {}", entityId, entityName);
			entityCache.evict(entityName, entityId);
		}
	}

	public void evictAll(String entityName)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			LOG.trace("Removing all entities from L1 cache that belong to {}", entityName);
			entityCache.evictAll(entityName);
		}
	}

	/**
	 * Retrieves an entity from the L1 cache based on a combination of entity name and entity id.
	 *
	 * @param entityName name of the entity to retrieve
	 * @param id         id value of the entity to retrieve
	 * @return the retrieved {@link Entity} or null if none found
	 */
	public Entity get(String entityName, Object id, EntityMetaData entityMetaData)
	{
		Optional<Entity> result = Optional.ofNullable(caches.get())
				.flatMap(cache -> cache.getIfPresent(entityMetaData, id));
		if (result.isPresent())
		{
			LOG.debug("Retrieved entity [{}] from L1 cache that belongs to {}", id, entityName);
		}
		else
		{
			LOG.trace("No entity with id [{}] present in L1 cache that belongs to {}", id, entityName);
		}
		return result.orElse(null);
	}

	/**
	 * Puts an entity into the L1 cache, if the cache exists for the current thread.
	 *
	 * @param entityName name of the entity to put into the cache
	 * @param entity     the entity to put into the cache
	 */
	public void put(String entityName, Entity entity)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			entityCache.put(entity);
			LOG.trace("Added dehydrated row [{}] from entity {} to the L1 cache", entity.getIdValue(), entityName);
		}
	}
}
