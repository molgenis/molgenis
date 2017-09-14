package org.molgenis.data.cache.l1;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.Cache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.cache.utils.CombinedEntityCache;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.TransactionManager;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

	public L1Cache(TransactionManager transactionManager, EntityHydration entityHydration)
	{
		caches = new ThreadLocal<>();
		this.entityHydration = requireNonNull(entityHydration);
		requireNonNull(transactionManager).addTransactionListener(this);
	}

	@Override
	public void transactionStarted(String transactionId)
	{
		LOG.trace("Creating L1 cache for transaction [{}]", transactionId);
		caches.set(createCache());
	}

	private CombinedEntityCache createCache()
	{
		Cache<EntityKey, Optional<Map<String, Object>>> cache = CaffeinatedGuava.build(
				Caffeine.newBuilder().maximumSize(MAX_CACHE_SIZE).recordStats());
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

	public void putDeletion(EntityKey entityKey)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			entityCache.putDeletion(entityKey);
		}
	}

	//TODO: Call this also when repository metadata changes!
	public void evictAll(EntityType entityType)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			LOG.trace("Removing all entities from L1 cache that belong to {}", entityType.getId());
			entityCache.evictAll(entityType);
		}
	}

	public void evict(Stream<EntityKey> entityKeys)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			LOG.trace("Removing entity keys from L1 cache.");
			entityCache.evict(entityKeys);
		}
	}

	/**
	 * Retrieves an entity from the L1 cache based on a combination of entity name and entity id.
	 *
	 * @param entityTypeId name of the entity to retrieve
	 * @param id           id value of the entity to retrieve
	 * @return the retrieved {@link Entity} or Optional.empty() if deletion of this entity is stored in the cache or
	 * null if no information available about this entity in the cache
	 */
	@SuppressFBWarnings(value = "NP_OPTIONAL_RETURN_NULL", justification = "Intentional behavior")
	public Optional<Entity> get(String entityTypeId, Object id, EntityType entityType)
	{
		CombinedEntityCache cache = caches.get();
		if (cache == null)
		{
			return null;
		}
		Optional<Entity> result = cache.getIfPresent(entityType, id);
		if (result != null)
		{
			LOG.debug("Retrieved entity [{}] from L1 cache that belongs to {}", id, entityTypeId);
		}
		else
		{
			LOG.trace("No entity with id [{}] present in L1 cache that belongs to {}", id, entityTypeId);
		}
		return result;
	}

	/**
	 * Puts an entity into the L1 cache, if the cache exists for the current thread.
	 *
	 * @param entityTypeId name of the entity to put into the cache
	 * @param entity       the entity to put into the cache
	 */
	public void put(String entityTypeId, Entity entity)
	{
		CombinedEntityCache entityCache = caches.get();
		if (entityCache != null)
		{
			entityCache.put(entity);
			LOG.trace("Added dehydrated row [{}] from entity {} to the L1 cache", entity.getIdValue(), entityTypeId);
		}
	}
}