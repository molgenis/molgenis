package org.molgenis.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.SetMultimap;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.support.*;
import org.molgenis.util.BatchingIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityTypeUtils.isSingleReferenceType;

/**
 * Entity manager responsible for creating entities, entity references and resolving references of reference attributes.
 */
@Component
public class EntityManagerImpl implements EntityManager
{
	private static final int BATCH_SIZE = 100;

	private final DataService dataService;
	private final EntityFactoryRegistry entityFactoryRegistry;
	private final EntityPopulator entityPopulator;

	@Autowired
	public EntityManagerImpl(DataService dataService, EntityFactoryRegistry entityFactoryRegistry,
			EntityPopulator entityPopulator)
	{
		this.dataService = requireNonNull(dataService);
		this.entityFactoryRegistry = requireNonNull(entityFactoryRegistry);
		this.entityPopulator = requireNonNull(entityPopulator);
	}

	@Override
	public Entity create(EntityType entityType, CreationMode creationMode)
	{
		return create(entityType, null, creationMode);
	}

	@Override
	public Entity createFetch(EntityType entityType, Fetch fetch)
	{
		return create(entityType, fetch, NO_POPULATE);
	}

	private Entity create(EntityType entityType, Fetch fetch, CreationMode creationMode)
	{
		Entity entity = new DynamicEntity(entityType);
		if (fetch != null)
		{
			// create partial entity that loads attribute values not contained in the fetch on demand.
			entity = new PartialEntity(entity, fetch, this);
		}

		if (entityType.hasAttributeWithExpression())
		{
			// create entity that computed values based on expressions defined in meta data
			entity = new EntityWithComputedAttributes(entity);
		}

		if (creationMode == POPULATE)
		{
			entityPopulator.populate(entity);
		}

		EntityFactory<? extends Entity, ?> entityFactory = entityFactoryRegistry.getEntityFactory(entityType);
		if (entityFactory != null)
		{
			// create static entity (e.g. Tag, Language, Package) that wraps the constructed dynamic or partial entity.
			return entityFactory.create(entity);
		}
		return entity;
	}

	@Override
	public Entity getReference(EntityType entityType, Object id)
	{
		Entity lazyEntity = new LazyEntity(entityType, dataService, id);

		EntityFactory<? extends Entity, ?> entityFactory = entityFactoryRegistry.getEntityFactory(entityType);
		if (entityFactory != null)
		{
			// create static entity (e.g. Tag, Language, Package) that wraps the constructed dynamic or partial entity.
			lazyEntity = entityFactory.create(lazyEntity);
		}

		return lazyEntity;
	}

	@Override
	public Iterable<Entity> getReferences(EntityType entityType, Iterable<?> ids)
	{
		EntityFactory<? extends Entity, ?> entityFactory = entityFactoryRegistry.getEntityFactory(entityType);
		return () -> stream(ids.spliterator(), false).map(id ->
		{
			Entity lazyEntity = getReference(entityType, id);
			if (entityFactory != null)
			{
				// create static entity (e.g. Tag, Language, Package) that wraps the constructed dynamic or partial entity.
				lazyEntity = entityFactory.create(lazyEntity);
			}
			return lazyEntity;
		}).iterator();
	}

	@Override
	public Entity resolveReferences(EntityType entityType, Entity entity, Fetch fetch)
	{
		Iterable<Entity> entities = resolveReferences(entityType, singletonList(entity), fetch);
		return entities.iterator().next();
	}

	private Iterable<Entity> resolveReferences(EntityType entityType, Iterable<Entity> entities, Fetch fetch)
	{
		// resolve lazy entity collections without references
		if (entities instanceof EntityCollection && ((EntityCollection) entities).isLazy())
		{
			// TODO remove cast after updating DataService/Repository interfaces to return EntityCollections
			return () -> dataService.findAll(entityType.getName(), new EntityIdIterable(entities).stream(), fetch)
					.iterator();
		}

		// no fetch exists that described what to resolve
		if (fetch == null)
		{
			return entities;
		}
		List<Attribute> resolvableAttrs = getResolvableAttrs(entityType, fetch);

		// entity has no references, nothing to resolve
		if (resolvableAttrs.isEmpty())
		{
			return entities;
		}

		// resolve entity references in batch since we need to do some bookkeeping
		final Iterable<Entity> batchingEntities = entities;
		return new BatchingIterable<Entity>(BATCH_SIZE)
		{
			private Iterator<List<Entity>> it;

			@Override
			public Iterator<Entity> iterator()
			{
				it = Iterators.partition(batchingEntities.iterator(), BATCH_SIZE);
				return super.iterator();
			}

			@Override
			protected Iterable<Entity> getBatch(int offset, int batchSize)
			{
				List<Entity> entities = it.hasNext() ? it.next() : Collections.emptyList();
				if (entities.isEmpty())
				{
					return entities;
				}
				else
				{
					return resolveReferences(resolvableAttrs, entities, fetch);
				}
			}
		};
	}

	@Override
	public Stream<Entity> resolveReferences(EntityType entityType, Stream<Entity> entities, Fetch fetch)
	{
		// resolve lazy entity collections without references
		if (entities instanceof EntityStream && ((EntityStream) entities).isLazy())
		{
			// TODO remove cast after updating DataService/Repository interfaces to return EntityStream
			return dataService.findAll(entityType.getName(), entities.map(Entity::getIdValue), fetch);
		}

		// no fetch exists that described what to resolve
		if (fetch == null)
		{
			return entities;
		}
		List<Attribute> resolvableAttrs = getResolvableAttrs(entityType, fetch);

		// entity has no references, nothing to resolve
		if (resolvableAttrs.isEmpty())
		{
			return entities;
		}

		Iterable<List<Entity>> iterable = () -> Iterators.partition(entities.iterator(), BATCH_SIZE);
		return stream(iterable.spliterator(), false).flatMap(batch ->
		{
			List<Entity> batchWithReferences = resolveReferences(resolvableAttrs, batch, fetch);
			return batchWithReferences.stream();
		});
	}

	private List<Entity> resolveReferences(List<Attribute> resolvableAttrs, List<Entity> entities, Fetch fetch)
	{
		// entity name --> entity ids
		SetMultimap<String, Object> lazyRefEntityIdsMap = HashMultimap.create(resolvableAttrs.size(), 16);
		// entity name --> attributes referring to this entity
		SetMultimap<String, Attribute> refEntityAttrsMap = HashMultimap.create(resolvableAttrs.size(), 2);

		// fill maps
		for (Attribute attr : resolvableAttrs)
		{
			String refEntityName = attr.getRefEntity().getName();

			if (isSingleReferenceType(attr))
			{
				for (Entity entity : entities)
				{
					Entity lazyRefEntity = entity.getEntity(attr.getName());
					if (lazyRefEntity != null)
					{
						lazyRefEntityIdsMap.put(refEntityName, lazyRefEntity.getIdValue());
					}
				}

			}
			else if (isMultipleReferenceType(attr))
			{
				for (Entity entity : entities)
				{
					Iterable<Entity> lazyRefEntities = entity.getEntities(attr.getName());
					for (Entity lazyRefEntity : lazyRefEntities)
					{
						lazyRefEntityIdsMap.put(refEntityName, lazyRefEntity.getIdValue());
					}
				}
			}

			refEntityAttrsMap.put(refEntityName, attr);
		}

		// batch retrieve referred entities and replace entity references with actual entities
		for (Entry<String, Collection<Object>> entry : lazyRefEntityIdsMap.asMap().entrySet())
		{
			String refEntityName = entry.getKey();

			// create a fetch for the referenced entity which is a union of the fetches defined by attributes
			// referencing this entity
			Set<Attribute> attrs = refEntityAttrsMap.get(refEntityName);
			Fetch subFetch = createSubFetch(fetch, attrs);

			// retrieve referenced entities
			Stream<Entity> refEntities = dataService.findAll(refEntityName, entry.getValue().stream(), subFetch);

			Map<Object, Entity> refEntitiesIdMap = refEntities
					.collect(Collectors.toMap(Entity::getIdValue, Function.identity()));

			for (Attribute attr : attrs)
			{
				if (isSingleReferenceType(attr))
				{
					String attrName = attr.getName();
					for (Entity entity : entities)
					{
						Entity lazyRefEntity = entity.getEntity(attrName);
						if (lazyRefEntity != null)
						{
							// replace lazy entity with real entity
							Object refEntityId = lazyRefEntity.getIdValue();
							Entity refEntity = refEntitiesIdMap.get(refEntityId);
							entity.set(attrName, refEntity);
						}
					}
				}
				else if (isMultipleReferenceType(attr))
				{
					String attrName = attr.getName();
					for (Entity entity : entities)
					{
						// replace lazy entities with real entities
						Iterable<Entity> lazyRefEntities = entity.getEntities(attrName);
						List<Entity> mrefEntities = stream(lazyRefEntities.spliterator(), true).map(lazyRefEntity ->
						{
							// replace lazy entity with real entity
							Object refEntityId = lazyRefEntity.getIdValue();
							return refEntitiesIdMap.get(refEntityId);
						}).collect(Collectors.toList());
						entity.set(attrName, mrefEntities);
					}
				}
			}
		}
		return entities;
	}

	private static Fetch createSubFetch(Fetch fetch, Iterable<Attribute> attrs)
	{
		Fetch subFetch = null;
		for (Attribute attr : attrs)
		{
			Fetch attrSubFetch = fetch.getFetch(attr.getName());
			if (attrSubFetch != null)
			{
				// lazy creation
				if (subFetch == null)
				{
					subFetch = new Fetch();
				}

				for (Entry<String, Fetch> entry : attrSubFetch)
				{
					mergeFetches(subFetch, entry.getKey(), entry.getValue());
				}
			}
			else
			{
				// prefer null value (=fetch all attributes) above other values (=filter some attributes)
				subFetch = null;
				break;
			}
		}
		return subFetch;
	}

	private static void mergeFetches(Fetch fetch, String field, Fetch subFetch)
	{
		if (subFetch == null)
		{
			// prefer null value above specific value
			fetch.field(field, null);
		}
		else if (fetch.hasField(field))
		{
			Fetch existingSubFetch = fetch.getFetch(field);
			if (existingSubFetch != null)
			{
				for (Map.Entry<String, Fetch> entry : subFetch)
				{
					mergeFetches(existingSubFetch, entry.getKey(), entry.getValue());
				}
			}
		}
		else
		{
			// first value for this field
			fetch.field(field, subFetch);
		}
	}

	/**
	 * Return all resolvable attributes: non-computed reference attributes defined in fetch
	 *
	 * @param entityType entity meta data
	 * @param fetch      entity fetch
	 * @return resolved attributes
	 */
	private static List<Attribute> getResolvableAttrs(EntityType entityType, Fetch fetch)
	{
		return stream(entityType.getAtomicAttributes().spliterator(), false).filter(EntityTypeUtils::isReferenceType)
				.filter(attr -> attr.getExpression() == null).filter(attr -> fetch.hasField(attr.getName()))
				.collect(Collectors.toList());
	}

	private static class EntityIdIterable implements Iterable<Object>
	{
		private final Iterable<Entity> entities;

		EntityIdIterable(Iterable<Entity> entities)
		{
			this.entities = requireNonNull(entities);
		}

		@Override
		public Iterator<Object> iterator()
		{
			return stream().iterator();
		}

		public Stream<Object> stream()
		{
			return StreamSupport.stream(entities.spliterator(), false).map(Entity::getIdValue);
		}
	}
}
