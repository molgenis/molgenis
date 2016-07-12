package org.molgenis.data.cache.l2;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.cache.utils.EntityHydration;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.transaction.DefaultMolgenisTransactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.Optional.empty;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.DataConverter.convert;

@Service
public class L2Cache extends DefaultMolgenisTransactionListener
{
	public static final Logger LOG = LoggerFactory.getLogger(L2Cache.class);
	private static final int MAX_CACHE_SIZE_PER_ENTITY = 1000;
	private final ConcurrentMap<String, LoadingCache<String, Optional<Map<String, Object>>>> caches;
	private final EntityHydration entityHydration;

	@Autowired
	public L2Cache(EntityHydration entityHydration)
	{
		this.entityHydration = Objects.requireNonNull(entityHydration);
		caches = Maps.newConcurrentMap();
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
		LoadingCache<String, Optional<Map<String, Object>>> cache = getEntityCache(repository);
		EntityMetaData entityMetaData = repository.getEntityMetaData();
		return cache.getUnchecked(id.toString()).map(e -> entityHydration.hydrate(e, entityMetaData)).orElse(null);
	}

	/**
	 * Retrieves a list of entities from the cache.
	 *
	 * @param repository
	 * @param ids
	 * @return
	 */
	public List<Entity> getBatch(Repository<Entity> repository, Iterable<Object> ids)
	{
		try
		{
			Iterable<String> idStrings = Iterables.transform(ids, Object::toString);
			Stream<Map<String, Object>> presentEntities = getEntityCache(repository).getAll(idStrings).values().stream()
					.filter(Optional::isPresent).map(Optional::get);
			return presentEntities.map(e -> entityHydration.hydrate(e, repository.getEntityMetaData()))
					.collect(Collectors.toList());
		}
		catch (ExecutionException exception)
		{
			rethrowUnchecked(exception);
			return null;
		}
	}

	@Scheduled(fixedRate = 60000)
	public void logStatistics()
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug("Cache stats:");
			for (Map.Entry<String, LoadingCache<String, Optional<Map<String, Object>>>> cacheEntry : caches.entrySet())
			{
				LOG.debug("{}:{}", cacheEntry.getKey(), cacheEntry.getValue().stats());
			}
		}
	}

	private void rethrowUnchecked(Exception exception)
	{
		if (exception.getCause() != null && exception.getCause() instanceof RuntimeException)
		{
			throw (RuntimeException) exception.getCause();
		}
		throw new MolgenisDataException(exception);
	}

	private LoadingCache<String, Optional<Map<String, Object>>> getEntityCache(Repository<Entity> repository)
	{
		String name = repository.getName();
		if (!caches.containsKey(name))
		{
			caches.putIfAbsent(name, createEntityCache(repository));
		}
		return caches.get(name);
	}

	private LoadingCache<String, Optional<Map<String, Object>>> createEntityCache(Repository<Entity> repository)
	{
		AttributeMetaData idAttribute = repository.getEntityMetaData().getIdAttribute();
		return newBuilder().recordStats().maximumSize(MAX_CACHE_SIZE_PER_ENTITY).expireAfterAccess(10, MINUTES)
				.build(createCacheLoader(repository, idAttribute));
	}

	private CacheLoader<String, Optional<Map<String, Object>>> createCacheLoader(final Repository<Entity> repository,
			final AttributeMetaData idAttribute)
	{
		return new CacheLoader<String, Optional<Map<String, Object>>>()
		{
			public Optional<Map<String, Object>> load(String key)
			{
				return Optional.ofNullable(repository.findOneById(key)).map(entityHydration::dehydrate);
			}

			@Override
			public Map<String, Optional<Map<String, Object>>> loadAll(Iterable<? extends String> keys)
			{
				Stream<Object> typedIds = stream(keys.spliterator(), false).map(this::getIdObject);
				Map<String, Optional<Map<String, Object>>> result = repository.findAll(typedIds)
						.collect(toMap(this::getIdString, this::dehydrateEntity));
				for (String key : keys)
				{
					// add empty entry for ids not present in the backend
					result.putIfAbsent(key, empty());
				}
				return result;
			}

			private Object getIdObject(String idString)
			{
				return convert(idString, idAttribute);
			}

			private String getIdString(Entity entity)
			{
				return entity.getIdValue().toString();
			}

			private Optional<Map<String, Object>> dehydrateEntity(Entity entity)
			{
				return Optional.of(entityHydration.dehydrate(entity));
			}
		};
	}
}
