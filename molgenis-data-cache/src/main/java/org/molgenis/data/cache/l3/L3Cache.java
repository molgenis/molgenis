package org.molgenis.data.cache.l3;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityKey;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.molgenis.data.transaction.TransactionInformation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.slf4j.LoggerFactory.getLogger;

public class L3Cache extends DefaultMolgenisTransactionListener
{
	public static final Logger LOG = getLogger(L3Cache.class);
	private static final int MAX_CACHE_SIZE_PER_QUERY = 1000;

	private final ConcurrentMap<String, LoadingCache<Query<Entity>, List<Object>>> caches;
	private final TransactionInformation transactionInformation;

	@Autowired
	public L3Cache(TransactionInformation transactionInformation)
	{
		this.transactionInformation = transactionInformation;
		caches = newConcurrentMap();
	}

	@Override
	public void afterCommitTransaction(String transactionId)
	{
		// TODO cleanup dirty repositories
		// Is this correct?
		transactionInformation.getDirtyRepositories().forEach(caches::remove);
		transactionInformation.getDirtyEntities().forEach(this::evict);
	}

	private void evict(EntityKey entityKey)
	{
		LoadingCache<Query<Entity>, List<Object>> cache = caches.get(entityKey.getEntityName());
		if (cache != null)
		{
			cache.invalidate(entityKey.getId());
		}
	}

	public void put(Repository<Entity> repository, Query<Entity> query, Stream<Object> ids)
	{

	}

	public List<Object> get(Repository<Entity> repository, Query<Entity> query)
	{
		LoadingCache<Query<Entity>, List<Object>> cache = getQueryCache(repository, query);
		return cache.getUnchecked(query);
	}

	private LoadingCache<Query<Entity>, List<Object>> getQueryCache(Repository<Entity> repository, Query<Entity> query)
	{
		String name = repository.getName();
		if (!caches.containsKey(name))
		{
			caches.putIfAbsent(name, createQueryCache(repository, query));
		}
		return caches.get(name);
	}

	private LoadingCache<Query<Entity>, List<Object>> createQueryCache(Repository<Entity> repository,
			Query<Entity> query)
	{
		return newBuilder().recordStats().maximumSize(MAX_CACHE_SIZE_PER_QUERY).expireAfterAccess(10, MINUTES)
				.build(createCacheLoader(query));
	}

	//	public Entity get(Repository<Entity> repository, Query<Entity> query)
	//	{
	//		LoadingCache<Query<Entity>, List<Object>> cache = getQueryCache(repository);
	//		EntityMetaData entityMetaData = repository.getEntityMetaData();
	//		return cache.getUnchecked(id.toString()).map(e -> entityHydration.hydrate(e, entityMetaData)).orElse(null);
	//	}
	//
	//	private LoadingCache<Query<Entity>, List<Object>> getQueryCache(Repository<Entity> repository)
	//	{
	//		String name = repository.getName();
	//		if (!caches.containsKey(name))
	//		{
	//			caches.putIfAbsent(name, createQueryCache(repository));
	//		}
	//		return caches.get(name);
	//	}
	//
	//	private LoadingCache<Object, Optional<Map<String, Object>>> createQueryCache(Repository<Entity> repository)
	//	{
	//		return newBuilder().recordStats().maximumSize(MAX_CACHE_SIZE_PER_ENTITY).expireAfterAccess(10, MINUTES)
	//				.build(createCacheLoader(repository));
	//	}

	/**
	 * Create a cacheloader that loads entity ids from the repository and stores them together with their query
	 *
	 * @return
	 */
	private CacheLoader<Query<Entity>, List<Object>> createCacheLoader(final Repository<Entity> repository,
			final Query<Entity> query)
	{
		return new CacheLoader<Query<Entity>, Optional<List<Object>>>()
		{
			/**
			 * Loads a single Id from an entity found with the supplied query
			 * @param key
			 * @return
			 * @throws Exception
			 */
			@Override
			public Optional<List<Object>> load(Query<Entity> key) throws Exception
			{
				return Optional.ofNullable(newArrayList(repository.findOne(query).getIdValue()));
			}

			/**
			 * Loads multiple Ids from the repository
			 * @param keys
			 * @return
			 * @throws Exception
			 */
			@Override
			public Map<Query<Entity>, List<Object>> loadAll(Iterable<? extends Query<Entity>> keys) throws Exception
			{

				return super.loadAll(keys);
			}
		};
	}
}
