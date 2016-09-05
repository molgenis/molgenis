package org.molgenis.data;

import autovalue.shaded.com.google.common.common.collect.Lists;
import autovalue.shaded.com.google.common.common.collect.Maps;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

/**
 * For attributes that are mapped by another attribute in another entity (e.g. entity 'author' with attribute 'book' of
 * type 'one-to-many' mapped by attribute 'author' of entity 'book') and vice versa, handle updates to the other side
 * of the bidirectional relationship.
 * <p>
 * TODO improve performance of streaming add/update/delete methods
 * TODO support bidirectional many-to-many relationships
 */
public class BidirectionalAttributeUpdateDecorator extends AbstractRepositoryDecorator<Entity>
{
	private final Repository<Entity> decoratedRepo;
	private final DataService dataService;

	public BidirectionalAttributeUpdateDecorator(Repository<Entity> decoratedRepo, DataService dataService)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	protected Repository<Entity> delegate()
	{
		return decoratedRepo;
	}

	@Override
	public void add(Entity entity)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			Map<String, List<Entity>> updatedRefEntitiesMap = Maps.newHashMap();
			determineInversedByUpdatesForAdd(entity, updatedRefEntitiesMap);
			determineMappedByUpdatesForAdd(entity, updatedRefEntitiesMap);

			// add entity
			super.add(entity);

			// update referenced entities
			updatedRefEntitiesMap.forEach((entityName, entities) ->
			{
				if (entities.size() == 1)
				{
					dataService.update(entityName, entities.get(0));
				}
				else
				{
					dataService.update(entityName, entities.stream());
				}
			});
		}
		else
		{
			super.add(entity);
		}
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			AtomicInteger count = new AtomicInteger(0);
			entities.forEach(entity ->
			{
				this.add(entity);
				count.incrementAndGet();
			});
			return count.get();
		}
		else
		{
			return super.add(entities);
		}
	}

	@Override
	public void update(Entity entity)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			Map<String, List<Entity>> updatedMappedByRefEntitiesMap = getMappedByUpdatesForUpdate(entity);
			Map<String, List<Entity>> updatedInversedByRefEntitiesMap = getInversedByUpdatesForUpdate(entity);

			// update referenced entities for mapped by attributes
			updatedMappedByRefEntitiesMap.forEach((entityName, entities) ->
			{
				if (entities.size() == 1)
				{
					dataService.update(entityName, entities.get(0));
				}
				else
				{
					dataService.update(entityName, entities.stream());
				}
			});

			// update entity
			super.update(entity);

			// update referenced entities for inversed by attributes
			updatedInversedByRefEntitiesMap.forEach((entityName, entities) ->
			{
				if (entities.size() == 1)
				{
					dataService.update(entityName, entities.get(0));
				}
				else
				{
					dataService.update(entityName, entities.stream());
				}
			});
		}
		else
		{
			super.update(entity);
		}
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			entities.forEach(this::update);
		}
		else
		{
			super.update(entities);
		}
	}

	@Override
	public void delete(Entity entity)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			AtomicBoolean doUpdateBeforeDelete = new AtomicBoolean(false);
			getEntityMetaData().getInversedByAttributes().forEach(attr ->
			{
				Entity refEntity = entity.getEntity(attr.getName());
				if (refEntity != null)
				{
					entity.set(attr.getName(), null);
					doUpdateBeforeDelete.set(true);
				}
			});

			getEntityMetaData().getMappedByAttributes().forEach(attr ->
			{
				List<Entity> refEntities = Lists.newArrayList(entity.getEntities(attr.getName()));
				if (!refEntities.isEmpty())
				{
					entity.set(attr.getName(), emptyList());
					doUpdateBeforeDelete.set(true);
				}
			});

			if (doUpdateBeforeDelete.get())
			{
				this.update(entity);
			}

			// delete entity
			super.delete(entity);
		}
		else
		{
			super.delete(entity);
		}
	}

	@Override
	public void deleteById(Object id)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			this.delete(findOneById(id));
		}
		else
		{
			super.deleteById(id);
		}
	}

	@Override
	public void deleteAll()
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			this.forEach(this::delete);
		}
		else
		{
			super.deleteAll();
		}
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			entities.forEach(this::delete);
		}
		else
		{
			super.delete(entities);
		}
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		if (getEntityMetaData().hasBidirectionalAttributes())
		{
			this.findAll(ids).forEach(this::delete);
		}
		else
		{
			super.deleteAll(ids);
		}
	}

	/**
	 * For new entities:
	 * Determine attribute value updates for attributes mapped by an attribute in another entity
	 * e.g. if entity is of type 'author' with one-to-many 'books' and 'book' has xref 'author' update 'books'
	 *
	 * @param entity                entity
	 * @param updatedRefEntitiesMap map from entity name to updated referenced entities
	 */
	private void determineMappedByUpdatesForAdd(Entity entity, Map<String, List<Entity>> updatedRefEntitiesMap)
	{
		getEntityMetaData().getMappedByAttributes().forEach(attr ->
		{
			Iterable<Entity> refEntities = entity.getEntities(attr.getName());
			if (!Iterables.isEmpty(refEntities))
			{
				refEntities.forEach(refEntity -> refEntity.set(attr.getMappedBy(), entity));

				String refEntityName = attr.getRefEntity().getName();
				List<Entity> updatedRefEntities = updatedRefEntitiesMap.get(refEntityName);
				if (updatedRefEntities == null)
				{
					updatedRefEntities = Lists.newArrayList();
					updatedRefEntitiesMap.put(refEntityName, updatedRefEntities);
				}
				updatedRefEntities.addAll(Lists.newArrayList(refEntities));
			}
		});
	}

	/**
	 * For new entities:
	 * Determine attribute value updates for attributes inversed by an attribute in another entity
	 * e.g. if entity is of type 'book' with xref 'author' and 'author' has one-to-many 'books' update 'author'
	 *
	 * @param entity                entity
	 * @param updatedRefEntitiesMap map from entity name to updated referenced entities
	 */
	private void determineInversedByUpdatesForAdd(Entity entity, Map<String, List<Entity>> updatedRefEntitiesMap)
	{

		getEntityMetaData().getInversedByAttributes().forEach(attr ->
		{
			Entity refEntity = entity.getEntity(attr.getName());
			if (refEntity != null)
			{
				String refEntityName = attr.getRefEntity().getName();
				String refAttrName = attr.getInversedBy().getName();
				Iterable<Entity> entities = refEntity.getEntities(refAttrName);
				refEntity.set(refAttrName, Iterables.concat(entities, singleton(entity)));

				List<Entity> updatedRefEntities = updatedRefEntitiesMap.get(refEntityName);
				if (updatedRefEntities == null)
				{
					updatedRefEntities = Lists.newArrayList();
					updatedRefEntitiesMap.put(refEntityName, updatedRefEntities);
				}
				updatedRefEntities.add(refEntity);
			}
		});
	}

	/**
	 * For updated entities:
	 * Determine attribute value updates for attributes mapped by an attribute in another entity
	 * e.g. if entity is of type 'author' with one-to-many 'books' and 'book' has xref 'author' update 'books'
	 *
	 * @param entity entity
	 * @return map from entity name to updated referenced entities
	 */
	private Map<String, List<Entity>> getMappedByUpdatesForUpdate(Entity entity)
	{
		Map<String, List<Entity>> updatedMappedByRefEntitiesMap = Maps.newHashMap();

		// determine attribute value updates for attributes mapped by an attribute in another entity
		// e.g. if entity is of type 'author' with one-to-many 'books' and 'book' has xref 'author' update 'books'
		getEntityMetaData().getMappedByAttributes().forEach(attr ->
		{
			Entity existingEntity = findOneById(entity.getIdValue());
			Iterable<Entity> refEntities = entity.getEntities(attr.getName());
			Iterable<Entity> existingRefEntities = existingEntity.getEntities(attr.getName());

			Map<Object, Entity> entityIdMap = stream(refEntities.spliterator(), false)
					.collect(toMap(Entity::getIdValue, Function.identity()));
			Map<Object, Entity> existingEntityIdMap = stream(existingRefEntities.spliterator(), false)
					.collect(toMap(Entity::getIdValue, Function.identity()));

			Set<Object> addedEntityIds = Sets.difference(entityIdMap.keySet(), existingEntityIdMap.keySet());
			Set<Object> removedEntityIds = Sets.difference(existingEntityIdMap.keySet(), entityIdMap.keySet());
			if (!addedEntityIds.isEmpty() || !removedEntityIds.isEmpty())
			{
				String refEntityName = attr.getRefEntity().getName();
				List<Entity> updatedRefEntities = updatedMappedByRefEntitiesMap.get(refEntityName);
				if (updatedRefEntities == null)
				{
					updatedRefEntities = Lists.newArrayList();
					updatedMappedByRefEntitiesMap.put(refEntityName, updatedRefEntities);
				}

				if (!addedEntityIds.isEmpty())
				{
					addedEntityIds.stream().map(entityIdMap::get).map(refEntity ->
					{
						refEntity.set(attr.getMappedBy(), entity); // should we make a copy?
						return refEntity;
					}).forEach(updatedRefEntities::add);
				}

				if (!removedEntityIds.isEmpty())
				{
					removedEntityIds.stream().map(existingEntityIdMap::get).map(refEntity ->
					{
						refEntity.set(attr.getMappedBy(), null); // should we make a copy?
						return refEntity;
					}).forEach(updatedRefEntities::add);
				}
			}
		});
		return updatedMappedByRefEntitiesMap;
	}

	/**
	 * For updated entities:
	 * Determine attribute value updates for attributes inversed by an attribute in another entity
	 * e.g. if entity is of type 'book' with xref 'author' and 'author' has one-to-many 'books' update 'author'
	 *
	 * @param entity entity
	 * @return map from entity name to updated referenced entities
	 */
	private Map<String, List<Entity>> getInversedByUpdatesForUpdate(Entity entity)
	{
		Map<String, List<Entity>> updatedInversedByRefEntitiesMap = Maps.newHashMap();

		// determine attribute value updates for attributes inversed by an attribute in another entity
		// e.g. if entity is of type 'book' with xref 'author' and 'author' has one-to-many 'books' update 'author'
		getEntityMetaData().getInversedByAttributes().forEach(attr ->
		{
			Entity refEntity = entity.getEntity(attr.getName());
			Entity existingEntity = findOneById(entity.getIdValue());
			Entity existingRefEntity = existingEntity.getEntity(attr.getName());
			if ((refEntity != null || existingRefEntity != null) && (refEntity == null || existingRefEntity == null
					|| !refEntity.getIdValue().equals(existingRefEntity.getIdValue())))
			{
				String refEntityName = attr.getRefEntity().getName();
				List<Entity> updatedRefEntities = updatedInversedByRefEntitiesMap.get(refEntityName);
				if (updatedRefEntities == null)
				{
					updatedRefEntities = Lists.newArrayList();
					updatedInversedByRefEntitiesMap.put(refEntityName, updatedRefEntities);
				}

				if (existingRefEntity == null || (refEntity != null && !refEntity.getIdValue()
						.equals(existingRefEntity.getIdValue())))
				{
					Iterable<Entity> entities = refEntity.getEntities(attr.getInversedBy().getName());
					boolean contains = stream(entities.spliterator(), false)
							.anyMatch(inversedByEntity -> inversedByEntity.getIdValue().equals(entity.getIdValue()));
					if (!contains)
					{
						Iterable<Entity> updatedEntities = Iterables.concat(entities, singleton(entity));
						refEntity.set(attr.getInversedBy().getName(), updatedEntities);
						updatedRefEntities.add(refEntity);
					}
				}
				if (refEntity == null || (existingRefEntity != null && !refEntity.getIdValue()
						.equals(existingRefEntity.getIdValue())))
				{
					List<Entity> updatedEntities = Lists
							.newArrayList(existingRefEntity.getEntities(attr.getInversedBy().getName()));
					boolean contains = false;
					for (ListIterator<Entity> it = updatedEntities.listIterator(); it.hasNext(); )
					{
						if (it.next().getIdValue().equals(entity.getIdValue()))
						{
							it.remove();
							contains = true;
							break;
						}
					}
					if (contains)
					{
						existingRefEntity.set(attr.getInversedBy().getName(), updatedEntities);
						updatedRefEntities.add(existingRefEntity);
					}
				}
			}
		});
		return updatedInversedByRefEntitiesMap;
	}
}
