package org.molgenis.data.cache.L1;

import com.google.common.cache.Cache;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.cache.utils.CachingUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class L1Cache extends DefaultMolgenisTransactionListener
{
	private static final Logger LOG = getLogger(L1Cache.class);
	private final ThreadLocal<Cache<String, Map<String, Object>>> cache;
	private final EntityManager entityManager;

	@Autowired
	public L1Cache(MolgenisTransactionManager molgenisTransactionManager, EntityManager entityManager)
	{
		cache = new ThreadLocal<>();
		this.entityManager = requireNonNull(entityManager);
		requireNonNull(molgenisTransactionManager).addTransactionListener(this);
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		LOG.debug("Creating L1 cache for transaction [{}]", transactionId);
		cache.set(createCache(1000));
	}

	@Override
	public void doCleanupAfterCompletion(String transactionId)
	{
		Cache<String, Map<String, Object>> entityCache = cache.get();
		if (entityCache != null)
		{
			LOG.debug("Cleaning up L1 cache after transaction [{}]", transactionId);
			cache.remove();
		}
	}

	/**
	 * Evict entity from cache based on entity name and entity id combination
	 *
	 * @param entityName
	 * @param entityId
	 */
	public void evict(String entityName, Object entityId)
	{
		LOG.debug("Removing  entity [{}] from L1 cache that belongs to {}", entityId, entityName);
		Cache<String, Map<String, Object>> entityCache = cache.get();
		if (entityCache != null)
		{
			String key = generateCacheKey(entityName, entityId);
			entityCache.invalidate(key);
		}
	}

	public void evictAll(String entityName)
	{
		LOG.debug("Removing all entities from L1 cache that belong to {}", entityName);
		Cache<String, Map<String, Object>> entityCache = cache.get();
		if (entityCache != null)
		{
			String prefix = entityName + "__";
			entityCache.asMap().keySet().stream().filter(key -> key.startsWith(prefix))
					.forEach(entityCache::invalidate);
		}
	}

	/**
	 * Retrieves a dehydrated entity from the L1 cache based on a combination of entity name and entity id.
	 * returns a hydrated entity or null if there is no value present for the requested key
	 *
	 * @param entityName
	 * @param id
	 * @return
	 */
	public Entity get(String entityName, Object id, EntityMetaData entityMetaData)
	{
		Cache<String, Map<String, Object>> entityCache = cache.get();
		if (entityCache != null)
		{
			String key = generateCacheKey(entityName, id);
			Map<String, Object> dehydratedEntity = entityCache.getIfPresent(key);

			if (dehydratedEntity != null)
			{
				LOG.debug("Retrieving and hydrating a dehydrated entity with id [{}] from L1 cache", key);
				return hydrate(dehydratedEntity, entityMetaData, entityManager);
			}
			LOG.debug("No dehydrated entities with id [{}] present in L1 cache", key);
		}
		return null;
	}

	/**
	 * Puts a dehydrated entity into the L1 cache, with the key based on a combination of entity name and entity id.
	 *
	 * @param entityName
	 * @param entity
	 */
	public void put(String entityName, Entity entity)
	{
		LOG.debug("Adding row [{}] from entity {} to the L1 cache", entity.getIdValue(), entityName);
		Cache<String, Map<String, Object>> entityCache = cache.get();
		if (entityCache != null)
		{
			String key = generateCacheKey(entityName, entity.getIdValue());

			Map<String, Object> dehydratedEntity = dehydrate(entity);
			LOG.debug("Dehydrating entity with id [{}]", key);

			entityCache.put(key, dehydratedEntity);
			LOG.debug("Adding dehydrated entity [{}] to L1 Cache", key);
		}
	}
}
