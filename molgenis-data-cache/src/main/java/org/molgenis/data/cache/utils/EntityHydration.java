package org.molgenis.data.cache.utils;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.EntityMetaDataUtils.isMultipleReferenceType;
import static org.molgenis.data.support.EntityMetaDataUtils.isSingleReferenceType;

/**
 * Hydrates and dehydrates entities.
 */
@Component
public class EntityHydration
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityHydration.class);
	private final EntityManager entityManager;

	@Autowired
	public EntityHydration(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	/**
	 * Rehydrate an entity.
	 *
	 * @param entityMetaData   metadata of the entity to rehydrate
	 * @param dehydratedEntity map with key value pairs representing this entity
	 * @return hydrated entity
	 */
	public Entity hydrate(Map<String, Object> dehydratedEntity, EntityMetaData entityMetaData)
	{
		LOG.trace("Hydrating entity: {} for entity {}", dehydratedEntity, entityMetaData.getName());

		Entity hydratedEntity = entityManager.create(entityMetaData);
		entityMetaData.getAtomicAttributes().forEach(attr -> {

			String name = attr.getName();
			Object value = dehydratedEntity.get(name);
			if (value != null)
			{
				if (isMultipleReferenceType(attr))
				{
					// We can do this cast because during dehydration, mrefs and categorical mrefs are stored as a List of Object
					Iterable<Entity> referenceEntities = entityManager
							.getReferences(attr.getRefEntity(), (List<Object>) value);
					hydratedEntity.set(name, referenceEntities);
				}
				else if (isSingleReferenceType(attr))
				{
					Entity referenceEntity = entityManager.getReference(attr.getRefEntity(), value);
					hydratedEntity.set(name, referenceEntity);
				}
				else
				{
					hydratedEntity.set(name, value);
				}
			}

		});

		return hydratedEntity;
	}

	/**
	 * Creates a Map containing the values required to rebuild this entity.
	 * For references to other entities only stores the ids.
	 *
	 * @param entity the {@link Entity} to dehydrate
	 * @return Map representation of the entity
	 */
	public Map<String, Object> dehydrate(Entity entity)
	{
		LOG.trace("Dehydrating entity {}", entity);
		Map<String, Object> dehydratedEntity = newHashMap();
		EntityMetaData entityMetaData = entity.getEntityMetaData();

		entityMetaData.getAtomicAttributes().forEach(attribute -> {
			String name = attribute.getName();
			AttributeType type = attribute.getDataType();

			dehydratedEntity.put(name, getValueBasedOnType(entity, name, type));
		});

		return dehydratedEntity;
	}

	private Object getValueBasedOnType(Entity entity, String name, AttributeType type)
	{
		Object value;
		switch (type)
		{
			case CATEGORICAL:
			case XREF:
			case FILE:
				Entity xrefEntity = entity.getEntity(name);
				value = xrefEntity != null ? xrefEntity.getIdValue() : null;
				break;
			case CATEGORICAL_MREF:
			case MREF:
				List<Object> mrefIdentifiers = newArrayList();
				entity.getEntities(name).forEach(mrefEntity -> {
					if (mrefEntity != null) mrefIdentifiers.add(mrefEntity.getIdValue());
				});
				value = mrefIdentifiers;
				break;
			case DATE:
				Date date = entity.getUtilDate(name);
				value = date != null ? date : null;
				break;
			case DATE_TIME:
				Date dateTime = entity.getUtilDate(name);
				value = dateTime != null ? dateTime : null;
				break;
			case BOOL:
			case COMPOUND:
			case DECIMAL:
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case INT:
			case LONG:
			case SCRIPT:
			case STRING:
			case TEXT:
				value = entity.get(name);
				break;
			default:
				throw new RuntimeException(String.format("Unknown attribute type [%s]", type));
		}

		LOG.trace("Dehydrating attribute '{}' of type [{}] resulted in value: {}", name, type.toString(), value);
		return value;
	}
}
