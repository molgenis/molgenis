package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;

/**
 * Class for entities not defined in pre-existing Java classes
 *
 * @see StaticEntity
 */
public class DynamicEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	/**
	 * Entity meta data
	 */
	private final EntityMetaData entityMeta;

	/**
	 * Maps attribute names to values. Value class types are determined by attribute data type.
	 */
	private final Map<String, Object> values;

	/**
	 * Constructs an entity with the given entity meta data.
	 *
	 * @param entityMeta entity meta
	 */
	public DynamicEntity(EntityMetaData entityMeta)
	{
		this.entityMeta = requireNonNull(entityMeta);
		this.values = newHashMap();
	}

	/**
	 * Constructs an entity with the given entity meta data and initialized the entity with the given data.
	 *
	 * @param entityMeta entity meta
	 * @param values     map with attribute name-value pairs
	 */
	public DynamicEntity(EntityMetaData entityMeta, Map<String, Object> values)
	{
		this(entityMeta);
		values.forEach(this::set);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMeta;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return stream(entityMeta.getAtomicAttributes().spliterator(), false).map(AttributeMetaData::getName)::iterator;
	}

	@Override
	public Object getIdValue()
	{
		// abstract entities might not have an id attribute
		AttributeMetaData idAttr = entityMeta.getIdAttribute();
		return idAttr != null ? get(idAttr.getName()) : null;
	}

	@Override
	public void setIdValue(Object id)
	{
		AttributeMetaData idAttr = entityMeta.getIdAttribute();
		if (idAttr == null)
		{
			throw new IllegalArgumentException(
					format("Entity [%s] doesn't have an id attribute", entityMeta.getName()));
		}
		set(idAttr.getName(), id);
	}

	@Override
	public Object getLabelValue()
	{
		// abstract entities might not have an label attribute
		AttributeMetaData labelAttr = entityMeta.getLabelAttribute();
		return labelAttr != null ? get(labelAttr.getName()) : null;
	}

	@Override
	public Object get(String attrName)
	{
		return values.get(attrName);
	}

	@Override
	public String getString(String attrName)
	{
		return (String) get(attrName);
	}

	@Override
	public Integer getInt(String attrName)
	{
		return (Integer) get(attrName);
	}

	@Override
	public Long getLong(String attrName)
	{
		return (Long) get(attrName);
	}

	@Override
	public Boolean getBoolean(String attrName)
	{
		return (Boolean) get(attrName);
	}

	@Override
	public Double getDouble(String attrName)
	{
		return (Double) get(attrName);
	}

	@Override
	public Date getDate(String attrName)
	{
		Object value = get(attrName);
		return value != null ? new java.sql.Date(((java.util.Date) value).getTime()) : null;
	}

	@Override
	public java.util.Date getUtilDate(String attrName)
	{
		Object value = get(attrName);
		return value != null ? new java.util.Date(((java.util.Date) value).getTime()) : null;
	}

	@Override
	public Timestamp getTimestamp(String attrName)
	{
		Object value = get(attrName);
		return value != null ? new java.sql.Timestamp(((java.util.Date) value).getTime()) : null;
	}

	@Override
	public Entity getEntity(String attrName)
	{
		return (Entity) get(attrName);
	}

	@Override
	public <E extends Entity> E getEntity(String attrName, Class<E> clazz)
	{
		//noinspection unchecked
		return (E) get(attrName);
	}

	@Override
	public Iterable<Entity> getEntities(String attrName)
	{
		Object value = get(attrName);
		//noinspection unchecked
		return value != null ? (Iterable<Entity>) value : emptyList();
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attrName, Class<E> clazz)
	{
		Object value = get(attrName);
		//noinspection unchecked
		return value != null ? (Iterable<E>) value : emptyList();
	}

	@Override
	public void set(String attrName, Object value)
	{
		validateValueType(attrName, value);
		values.put(attrName, value);
	}

	@Override
	public void set(Entity values)
	{
		values.getAttributeNames().forEach(attrName -> set(attrName, values.get(attrName)));
	}

	/**
	 * Validate is value is of the type defined by the attribute data type.
	 *
	 * @param attrName attribute name
	 * @param value    value (must be of the type defined by the attribute data type.)
	 */
	protected void validateValueType(String attrName, Object value)
	{
		if (value == null)
		{
			return;
		}

		AttributeMetaData attr = entityMeta.getAttribute(attrName);
		if (attr == null)
		{
			throw new UnknownAttributeException(format("Unknown attribute [%s]", attrName));
		}

		MolgenisFieldTypes.AttributeType dataType = attr.getDataType();
		switch (dataType)
		{
			case BOOL:
				if (!(value instanceof Boolean))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), Boolean.class.getSimpleName()));
				}
				break;
			case CATEGORICAL:
				// expected type is FileMeta. validation is not possible because molgenis-data does not depend on molgenis-file
			case FILE:
			case XREF:
				if (!(value instanceof Entity))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), Entity.class.getSimpleName()));
				}
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				if (!(value instanceof Iterable))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), Iterable.class.getSimpleName()));
				}
				break;
			case COMPOUND:
				throw new IllegalArgumentException(format("Unexpected data type [%s]", dataType.toString()));
			case DATE:
			case DATE_TIME:
				if (!(value instanceof java.util.Date))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), java.util.Date.class.getSimpleName()));
				}
				break;
			case DECIMAL:
				if (!(value instanceof Double))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), Double.class.getSimpleName()));
				}
				break;
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				if (!(value instanceof String))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), String.class.getSimpleName()));
				}
				break;
			case INT:
				if (!(value instanceof Integer))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), Integer.class.getSimpleName()));
				}
				break;
			case LONG:
				if (!(value instanceof Long))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s]", value.toString(),
									value.getClass().getSimpleName(), Long.class.getSimpleName()));
				}
				break;
			default:
				throw new RuntimeException(format("Unknown data type [%s]", dataType.toString()));
		}
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder(entityMeta.getName()).append('{');
		strBuilder.append(stream(entityMeta.getAtomicAttributes().spliterator(), false).map(attr ->
		{
			StringBuilder attrStrBuilder = new StringBuilder(attr.getName()).append('=');
			if (EntityMetaDataUtils.isSingleReferenceType(attr))
			{
				Entity refEntity = getEntity(attr.getName());
				attrStrBuilder.append(refEntity != null ? refEntity.getIdValue() : null);
			}
			else if (EntityMetaDataUtils.isMultipleReferenceType(attr))
			{
				attrStrBuilder.append('[')
						.append(stream(getEntities(attr.getName()).spliterator(), false).map(Entity::getIdValue)
								.map(Object::toString).collect(joining(","))).append(']');
			}
			else
			{
				attrStrBuilder.append(get(attr.getName()));
			}
			return attrStrBuilder.toString();
		}).collect(Collectors.joining("&")));
		strBuilder.append('}');
		return strBuilder.toString();
	}
}
