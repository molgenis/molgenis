package org.molgenis.data.cache.l1;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.molgenis.data.*;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.collect.Iterators.partition;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.RepositoryCapability.CACHEABLE;
import static org.molgenis.data.RepositoryCapability.WRITABLE;

/**
 * Adds, removes and retrieves entities from the {@link L1Cache} when a {@link Repository} is {@link RepositoryCapability#CACHEABLE}.
 * Delegates to the underlying repository when an action is not supported by the cache or when the cache doesn't contain
 * the needed entity.
 */
public class L1CacheRepositoryDecorator extends AbstractRepositoryDecorator<Entity>
{
	private static final int ID_BATCH_SIZE = 1000;

	private final Repository<Entity> decoratedRepository;
	private final L1Cache l1Cache;

	private final boolean cacheable;

	public L1CacheRepositoryDecorator(Repository<Entity> decoratedRepository, L1Cache l1Cache)
	{
		this.decoratedRepository = requireNonNull(decoratedRepository);
		this.l1Cache = requireNonNull(l1Cache);
		this.cacheable = decoratedRepository.getCapabilities().containsAll(newArrayList(CACHEABLE, WRITABLE));
	}

	@Override
	public Repository<Entity> delegate()
	{
		return decoratedRepository;
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable)
		{
			String entityId = getEntityType().getId();
			entities = entities.peek(entity -> l1Cache.put(entityId, entity));
		}
		return delegate().add(entities);
	}

	@Override
	public void add(Entity entity)
	{
		evictBiDiReferencedEntities(entity);
		if (cacheable) l1Cache.put(getEntityType().getId(), entity);
		delegate().add(entity);
	}

	@Override
	public Entity findOneById(Object id)
	{
		if (cacheable)
		{
			Optional<Entity> entity = l1Cache.get(getEntityType().getId(), id, getEntityType());
			if (entity != null)
			{
				return entity.orElse(null);
			}
		}
		return delegate().findOneById(id);
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		if (cacheable)
		{
			Iterator<List<Object>> idBatches = partition(ids.iterator(), ID_BATCH_SIZE);
			Iterator<List<Entity>> entityBatches = Iterators.transform(idBatches, this::findAllBatch);
			return stream(spliteratorUnknownSize(entityBatches, SORTED | ORDERED), false).flatMap(List::stream)
																						 .filter(Objects::nonNull);
		}
		return delegate().findAll(ids);
	}

	/**
	 * Looks up the Entities for a List of entity IDs.
	 * Those present in the cache are returned from cache. The missing ones are retrieved from the decoratedRepository.
	 *
	 * @param batch list of entity IDs to look up
	 * @return List of {@link Entity}s
	 */
	private List<Entity> findAllBatch(List<Object> batch)
	{
		String entityId = getEntityType().getId();
		EntityType entityType = getEntityType();
		List<Object> missingIds = batch.stream()
									   .filter(id -> l1Cache.get(entityId, id, entityType) == null)
									   .collect(toList());

		Map<Object, Entity> missingEntities = delegate().findAll(missingIds.stream())
														.collect(toMap(Entity::getIdValue, e -> e));

		return Lists.transform(batch, id ->
		{
			Optional<Entity> result = l1Cache.get(entityId, id, getEntityType());
			if (result == null)
			{
				return missingEntities.get(id);
			}
			return result.orElse(null);
		});
	}

	@Override
	public void update(Entity entity)
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable) l1Cache.put(getEntityType().getId(), entity);
		delegate().update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable)
		{
			entities = entities.filter(entity ->
			{
				l1Cache.put(getEntityType().getId(), entity);
				return true;
			});
		}
		delegate().update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		evictBiDiReferencedEntities(entity);
		if (cacheable) l1Cache.putDeletion(EntityKey.create(entity));
		delegate().delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable) entities = entities.peek(entity -> l1Cache.putDeletion(EntityKey.create(entity)));
		delegate().delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable) l1Cache.putDeletion(EntityKey.create(getEntityType(), id));
		delegate().deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable)
		{
			EntityType entityType = getEntityType();
			ids = ids.peek(id -> l1Cache.putDeletion(EntityKey.create(entityType, id)));
		}
		delegate().deleteAll(ids);
	}

	@Override
	public void deleteAll()
	{
		evictBiDiReferencedEntityTypes();
		if (cacheable) l1Cache.evictAll(getEntityType());
		delegate().deleteAll();
	}

	/**
	 * Evict all entries for entity types referred to by this entity type through a bidirectional relation.
	 */
	private void evictBiDiReferencedEntityTypes()
	{
		getEntityType().getMappedByAttributes().map(Attribute::getRefEntity).forEach(l1Cache::evictAll);
		getEntityType().getInversedByAttributes().map(Attribute::getRefEntity).forEach(l1Cache::evictAll);
	}

	/**
	 * Evict all entity instances referenced by this entity instance through a bidirectional relation.
	 *
	 * @param entity the entity whose references need to be evicted
	 */
	private void evictBiDiReferencedEntities(Entity entity)
	{
		Stream<EntityKey> backreffingEntities = getEntityType().getMappedByAttributes()
															   .flatMap(mappedByAttr -> stream(
																	   entity.getEntities(mappedByAttr.getName())
																			 .spliterator(), false))
															   .map(EntityKey::create);
		Stream<EntityKey> manyToOneEntities = getEntityType().getInversedByAttributes()
															 .map(inversedByAttr -> entity.getEntity(
																	 inversedByAttr.getName()))
															 .filter(Objects::nonNull)
															 .map(EntityKey::create);

		l1Cache.evict(Stream.concat(backreffingEntities, manyToOneEntities));
	}

}
