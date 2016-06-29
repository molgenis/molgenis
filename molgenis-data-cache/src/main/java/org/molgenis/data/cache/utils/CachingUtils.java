package org.molgenis.data.cache.utils;

import com.google.common.cache.Cache;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.model.EntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;
import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Maps.newHashMap;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;

public class CachingUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(CachingUtils.class);

	/**
	 * Rebuild entity from a map with key value pairs representing this entity.
	 *
	 * @param dehydratedEntity
	 * @return hydrated entity
	 */
	public static Entity hydrate(Map<String, Object> dehydratedEntity, EntityMetaData entityMetaData,
			EntityManager entityManager)
	{
		LOG.trace("Hydrating entity: {} for entity {}", dehydratedEntity, entityMetaData.getName());

		Entity hydratedEntity = entityManager.create(entityMetaData);
		entityMetaData.getAtomicAttributes().forEach(attribute -> {

			String name = attribute.getName();
			Object value = dehydratedEntity.get(name);
			if (value != null)
			{
				AttributeType type = attribute.getDataType();

				if (type.equals(MREF) || type.equals(CATEGORICAL_MREF))
				{
					// We can do this cast because during dehydration, mrefs and categorical mrefs are stored as a List of Object
					Iterable<Entity> referenceEntities = entityManager
							.getReferences(entityMetaData, (List<Object>) value);
					hydratedEntity.set(name, referenceEntities);
				}
				else if (type.equals(XREF) || type.equals(CATEGORICAL) || type.equals(FILE))
				{
					Entity referenceEntity = entityManager.getReference(entityMetaData, value);
					hydratedEntity.set(name, referenceEntity);
				}
				else
				{
					hydratedEntity.set(name, value);
				}
			}

		});

		LOG.trace("Created a hydrated entity: {}", hydratedEntity);
		return hydratedEntity;
	}

	/**
	 * Do not store entity in the cache since it might be updated by client code, instead store the values requires to
	 * rebuild this entity. For references to other entities only store the ids.
	 *
	 * @param entity
	 * @return
	 */
	public static Map<String, Object> dehydrate(Entity entity)
	{
		LOG.trace("Dehydrating entity {}", entity);
		Map<String, Object> dehydratedEntity = newHashMap();
		EntityMetaData entityMetaData = entity.getEntityMetaData();

		entityMetaData.getAtomicAttributes().forEach(attribute -> {
			String name = attribute.getName();
			AttributeType type = attribute.getDataType();

			dehydratedEntity.put(name, getValueBasedonType(entity, name, type));
		});

		return dehydratedEntity;
	}

	public static Cache<String, Map<String, Object>> createCache(int maximumSize)
	{
		return newBuilder().maximumSize(maximumSize).build();
	}

	public static String generateCacheKey(String entityName, Object id)
	{
		return entityName + "__" + id.toString();
	}

	private static Object getValueBasedonType(Entity entity, String name, AttributeType type)
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
