package org.molgenis.data.cache.utils;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.EntityWithComputedAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
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
	 * Rehydrate an entity. Entity can be an {@link EntityWithComputedAttributes}
	 * if there are attributes present with an expression
	 *
	 * @param entityMetaData   metadata of the entity to rehydrate
	 * @param dehydratedEntity map with key value pairs representing this entity
	 * @return hydrated entity
	 */
	public Entity hydrate(Map<String, Object> dehydratedEntity, EntityMetaData entityMetaData)
	{
		LOG.trace("Hydrating entity: {} for entity {}", dehydratedEntity, entityMetaData.getName());

		Entity hydratedEntity = entityManager.create(entityMetaData);

		for (AttributeMetaData attribute : entityMetaData.getAtomicAttributes())
		{
			// Only hydrate the attribute if it is NOT computed.
			// Computed attributes will be calculated based on the metadata
			if (attribute.getExpression() == null)
			{
				String name = attribute.getName();
				Object value = dehydratedEntity.get(name);
				if (value != null)
				{
					if (isMultipleReferenceType(attribute))
					{
						// We can do this cast because during dehydration, mrefs and categorical mrefs are stored as a List of Object
						value = entityManager.getReferences(attribute.getRefEntity(), (List<Object>) value);
					}
					else if (isSingleReferenceType(attribute))
					{
						value = entityManager.getReference(attribute.getRefEntity(), value);
					}
				}
				hydratedEntity.set(name, value);
			}
		}

		return hydratedEntity;
	}

	/**
	 * Creates a Map containing the values required to rebuild this entity.
	 * For references to other entities only stores the ids.
	 *
	 * @param entity the {@link Entity} to dehydrate
	 * @return Map representation of the entity
	 */
	public static Map<String, Object> dehydrate(Entity entity)
	{
		LOG.trace("Dehydrating entity {}", entity);
		Map<String, Object> dehydratedEntity = newHashMap();
		EntityMetaData entityMetaData = entity.getEntityMetaData();

		entityMetaData.getAtomicAttributes().forEach(attribute ->
		{
			// Only dehydrate if the attribute is NOT computed
			if (!attribute.hasExpression())
			{
				String name = attribute.getName();
				AttributeType type = attribute.getDataType();

				dehydratedEntity.put(name, getValueBasedOnType(entity, name, type));
			}
		});

		return dehydratedEntity;
	}

	private static Object getValueBasedOnType(Entity entity, String name, AttributeType type)
	{
		Object value;
		switch (type)
		{
			case CATEGORICAL:
			case FILE:
			case XREF:
				Entity xrefEntity = entity.getEntity(name);
				value = xrefEntity != null ? xrefEntity.getIdValue() : null;
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				List<Object> mrefIdentifiers = newArrayList();
				entity.getEntities(name).forEach(mrefEntity ->
				{
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
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", type.toString()));
			default:
				throw new RuntimeException(String.format("Unknown attribute type [%s]", type));
		}

		LOG.trace("Dehydrating attribute '{}' of type [{}] resulted in value: {}", name, type.toString(), value);
		return value;
	}
}
