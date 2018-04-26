package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

import java.time.Instant;
import java.time.LocalDate;
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
	/**
	 * Entity meta data
	 */
	private final EntityType entityType;

	/**
	 * Maps attribute names to values. Value class types are determined by attribute data type.
	 */
	private final Map<String, Object> values;

	/**
	 * Constructs an entity with the given entity meta data.
	 *
	 * @param entityType entity meta
	 */
	public DynamicEntity(EntityType entityType)
	{
		this.entityType = requireNonNull(entityType);
		this.values = newHashMap();
	}

	/**
	 * Constructs an entity with the given entity meta data and initialized the entity with the given data.
	 *
	 * @param entityType entity meta
	 * @param values     map with attribute name-value pairs
	 */
	public DynamicEntity(EntityType entityType, Map<String, Object> values)
	{
		this(entityType);
		values.forEach(this::set);
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return stream(entityType.getAtomicAttributes().spliterator(), false).map(Attribute::getName)::iterator;
	}

	@Override
	public Object getIdValue()
	{
		// abstract entities might not have an id attribute
		Attribute idAttr = entityType.getIdAttribute();
		return idAttr != null ? get(idAttr.getName()) : null;
	}

	@Override
	public void setIdValue(Object id)
	{
		Attribute idAttr = entityType.getIdAttribute();
		if (idAttr == null)
		{
			throw new IllegalArgumentException(format("Entity [%s] doesn't have an id attribute", entityType.getId()));
		}
		set(idAttr.getName(), id);
	}

	@Override
	public Object getLabelValue()
	{
		// abstract entities might not have an label attribute
		Attribute labelAttr = entityType.getLabelAttribute();
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
	public Instant getInstant(String attrName)
	{
		return (Instant) get(attrName);
	}

	@Override
	public LocalDate getLocalDate(String attrName)
	{
		return (LocalDate) get(attrName);
	}

	@Override
	public Entity getEntity(String attrName)
	{
		return (Entity) get(attrName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E getEntity(String attrName, Class<E> clazz)
	{
		return (E) get(attrName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attrName)
	{
		Object value = get(attrName);
		return value != null ? (Iterable<Entity>) value : emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Iterable<E> getEntities(String attrName, Class<E> clazz)
	{
		Object value = get(attrName);
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

		Attribute attr = entityType.getAttribute(attrName);
		if (attr == null)
		{
			throw new UnknownAttributeException(entityType, attrName);
		}

		AttributeType dataType = attr.getDataType();
		switch (dataType)
		{
			case BOOL:
				if (!(value instanceof Boolean))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Boolean.class.getSimpleName(), attrName));
				}
				break;
			case CATEGORICAL:
				// expected type is FileMeta. validation is not possible because molgenis-data does not depend on molgenis-file
			case FILE:
			case XREF:
				if (!(value instanceof Entity))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Entity.class.getSimpleName(), attrName));
				}
				break;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				if (!(value instanceof Iterable))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Iterable.class.getSimpleName(), attrName));
				}
				break;
			case COMPOUND:
				throw new IllegalArgumentException(
						format("Unexpected data type [%s] for attribute: [%s]", dataType.toString(), attrName));
			case DATE:
				if (!(value instanceof LocalDate))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), LocalDate.class.getSimpleName(), attrName));
				}
				break;
			case DATE_TIME:
				if (!(value instanceof Instant))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Instant.class.getSimpleName(), attrName));
				}
				break;
			case DECIMAL:
				if (!(value instanceof Double))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Double.class.getSimpleName(), attrName));
				}
				if (((Double) value).isNaN())
				{
					throw new MolgenisDataException(
							format("Value [%s] for type [%s] is not allowed for attribute: [%s]", value.toString(),
									Double.class.getSimpleName(), attrName));
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
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), String.class.getSimpleName(), attrName));
				}
				break;
			case INT:
				if (!(value instanceof Integer))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Integer.class.getSimpleName(), attrName));
				}
				break;
			case LONG:
				if (!(value instanceof Long))
				{
					throw new MolgenisDataException(
							format("Value [%s] is of type [%s] instead of [%s] for attribute: [%s]", value.toString(),
									value.getClass().getSimpleName(), Long.class.getSimpleName(), attrName));
				}
				break;
			default:
				throw new UnexpectedEnumException(dataType);
		}
	}

	@Override
	public String toString()
	{
		StringBuilder strBuilder = new StringBuilder(entityType.getId()).append('{');
		strBuilder.append(stream(entityType.getAtomicAttributes().spliterator(), false).map(attr ->
		{
			StringBuilder attrStrBuilder = new StringBuilder(attr.getName()).append('=');
			if (EntityTypeUtils.isSingleReferenceType(attr))
			{
				Entity refEntity = getEntity(attr.getName());
				attrStrBuilder.append(refEntity != null ? refEntity.getIdValue() : null);
			}
			else if (EntityTypeUtils.isMultipleReferenceType(attr))
			{
				attrStrBuilder.append('[')
							  .append(stream(getEntities(attr.getName()).spliterator(), false).map(Entity::getIdValue)
																							  .map(Object::toString)
																							  .collect(joining(",")))
							  .append(']');
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
