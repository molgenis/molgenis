package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.molgenis.data.support.LazyEntity;
import org.molgenis.data.support.PartialEntity;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.BatchingIterable;
import org.molgenis.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterators;
import com.google.common.collect.SetMultimap;

/**
 * Entity manager responsible for creating entity references and resolving references of reference attributes.
 */
public class EntityManagerImpl implements EntityManager
{
	private static final int BATCH_SIZE = 100;

	private final DataService dataService;

	@Autowired
	public EntityManagerImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Entity getReference(EntityMetaData entityMeta, Object id)
	{
		return new LazyEntity(entityMeta, dataService, id);
	}

	@Override
	public Iterable<Entity> getReferences(EntityMetaData entityMeta, Iterable<?> ids)
	{
		return new LazyEntityIterable(entityMeta, ids);
	}

	@Override
	public Entity resolveReferences(EntityMetaData entityMeta, Entity entity, Fetch fetch)
	{
		Iterable<Entity> entities = resolveReferences(entityMeta, Arrays.asList(entity), fetch);
		return entities.iterator().next();
	}

	private Iterable<Entity> resolveReferences(EntityMetaData entityMeta, Iterable<Entity> entities, Fetch fetch)
	{
		// resolve lazy entity collections without references
		if (entities instanceof EntityCollection && ((EntityCollection) entities).isLazy())
		{
			// TODO remove cast after updating DataService/Repository interfaces to return EntityCollections
			return new Iterable<Entity>()
			{
				@Override
				public Iterator<Entity> iterator()
				{
					return dataService.findAll(entityMeta.getName(), new EntityIdIterable(entities).stream(), fetch)
							.iterator();
				}
			};
		}

		// no fetch exists that described what to resolve
		if (fetch == null)
		{
			return entities;
		}

		List<AttributeMetaData> resolvableAttrs = getResolvableAttrs(entityMeta, fetch);

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
	public Stream<Entity> resolveReferences(EntityMetaData entityMeta, Stream<Entity> entities, Fetch fetch)
	{
		// FIXME how to enable this optimization?
		// // resolve lazy entity collections without references
		// if (entities instanceof EntityCollection && ((EntityCollection) entities).isLazy())
		// {
		// // TODO remove cast after updating DataService/Repository interfaces to return EntityCollections
		// return dataService.findAll(entityMeta.getName(), new EntityIdIterable(entities), fetch);
		// }

		// no fetch exists that described what to resolve
		if (fetch == null)
		{
			return entities;
		}

		List<AttributeMetaData> resolvableAttrs = getResolvableAttrs(entityMeta, fetch);

		// entity has no references, nothing to resolve
		if (resolvableAttrs.isEmpty())
		{
			return entities;
		}

		Iterable<List<Entity>> iterable = () -> Iterators.partition(entities.iterator(), BATCH_SIZE);
		return stream(iterable.spliterator(), false).flatMap(batch -> {
			List<Entity> batchWithReferences = resolveReferences(resolvableAttrs, batch, fetch);
			return batchWithReferences.stream();
		});
	}

	private List<Entity> resolveReferences(List<AttributeMetaData> resolvableAttrs, List<Entity> entities, Fetch fetch)
	{
		// entity name --> entity ids
		SetMultimap<String, Object> lazyRefEntityIdsMap = HashMultimap.<String, Object> create(resolvableAttrs.size(),
				16);
		// entity name --> attributes referring to this entity
		SetMultimap<String, AttributeMetaData> refEntityAttrsMap = HashMultimap
				.<String, AttributeMetaData> create(resolvableAttrs.size(), 2);

		// fill maps
		for (AttributeMetaData attr : resolvableAttrs)
		{
			String refEntityName = attr.getRefEntity().getName();

			FieldType attrType = attr.getDataType();
			if (attrType instanceof XrefField)
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
			else if (attrType instanceof MrefField)
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
			Set<AttributeMetaData> attrs = refEntityAttrsMap.get(refEntityName);
			Fetch subFetch = createSubFetch(fetch, attrs);

			// retrieve referenced entities
			Stream<Entity> refEntities = dataService.findAll(refEntityName, entry.getValue().stream(), subFetch);

			Map<Object, Entity> refEntitiesIdMap = refEntities
					.collect(Collectors.toMap(Entity::getIdValue, Function.identity()));

			for (AttributeMetaData attr : attrs)
			{
				FieldType attrType = attr.getDataType();
				if (attrType instanceof XrefField)
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
				else if (attrType instanceof MrefField)
				{
					String attrName = attr.getName();
					for (Entity entity : entities)
					{
						// replace lazy entities with real entities
						Iterable<Entity> lazyRefEntities = entity.getEntities(attrName);
						List<Entity> mrefEntities = stream(lazyRefEntities.spliterator(), true).map(lazyRefEntity -> {
							// replace lazy entity with real entity
							Object refEntityId = lazyRefEntity.getIdValue();
							Entity refEntity = refEntitiesIdMap.get(refEntityId);
							return refEntity;
						}).collect(Collectors.toList());
						entity.set(attrName, mrefEntities);
					}
				}
			}
		}
		return entities;
	}

	private Fetch createSubFetch(Fetch fetch, Iterable<AttributeMetaData> attrs)
	{
		Fetch subFetch = null;
		for (AttributeMetaData attr : attrs)
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

	private void mergeFetches(Fetch fetch, String field, Fetch subFetch)
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
			else
			{
				// do nothing
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
	 * @param entityMeta
	 * @param fetch
	 * @return
	 */
	private List<AttributeMetaData> getResolvableAttrs(EntityMetaData entityMeta, Fetch fetch)
	{
		return stream(entityMeta.getAtomicAttributes().spliterator(), false)
				.filter(attr -> attr.getDataType() instanceof XrefField || attr.getDataType() instanceof MrefField)
				.filter(attr -> attr.getExpression() == null).filter(attr -> fetch.hasField(attr.getName()))
				.collect(Collectors.toList());
	}

	private class LazyEntityIterable implements Iterable<Entity>
	{
		private final EntityMetaData entityMeta;
		private final Iterable<?> entityIds;

		public LazyEntityIterable(EntityMetaData entityMeta, Iterable<?> ids)
		{
			this.entityMeta = requireNonNull(entityMeta);
			this.entityIds = requireNonNull(ids);
		}

		@Override
		public Iterator<Entity> iterator()
		{
			Stream<?> stream = stream(entityIds.spliterator(), false);
			return stream.map(id -> getReference(entityMeta, id)).collect(Collectors.toList()).iterator();
		}
	}

	private class EntityIdIterable implements Iterable<Object>
	{
		private final Iterable<Entity> entities;

		public EntityIdIterable(Iterable<Entity> entities)
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

	@Override
	public <E extends Entity> E convert(Entity entity, Class<E> entityClass)
	{
		return entity != null ? EntityUtils.convert(entity, entityClass, dataService) : null;
	}

	@Override
	public <E extends Entity> Iterable<E> convert(Iterable<Entity> entities, Class<E> entityClass)
	{
		return new Iterable<E>()
		{
			@Override
			public Iterator<E> iterator()
			{
				return stream(entities.spliterator(), false)
						.map(entity -> EntityUtils.convert(entity, entityClass, dataService)).iterator();
			}
		};
	}

	@Override
	public Entity createEntityForPartialEntity(Entity partialEntity, Fetch fetch)
	{
		if (fetch == null)
		{
			return partialEntity;
		}
		else
		{
			return new PartialEntity(partialEntity, fetch, this);
		}
	}
}
