package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

import java.time.Instant;
import java.time.LocalDate;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Entity reference containing the entity identifier. Throws an expection when retrieving or updating values
 * other than the entity identifier.
 *
 * @see Entity
 * @see LazyEntity
 */
public class EntityReference implements Entity
{
	private static final String ILLEGAL_VALUE_TYPE_MESSAGE = "Value [%s] is of type [%s] instead of [%s] for attribute: [%s]";

	private final EntityType entityType;
	private Object id;

	public EntityReference(EntityType entityType, Object id)
	{
		this.entityType = requireNonNull(entityType);
		this.id = requireNonNull(validateIdType(id));
	}

	@Override
	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return EntityTypeUtils.getAttributeNames(entityType.getAtomicAttributes());
	}

	@Override
	public Object getIdValue()
	{
		return id;
	}

	@Override
	public void setIdValue(Object id)
	{
		setIdValue(entityType.getIdAttribute().getName(), id);
	}

	@Override
	public Object getLabelValue()
	{
		Attribute labelAttribute = entityType.getLabelAttribute();
		return getIdValue(labelAttribute.getName());
	}

	@Override
	public Object get(String attributeName)
	{
		return getIdValue(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		return (String) getIdValue(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return (Integer) getIdValue(attributeName);
	}

	@Override
	public Long getLong(String attributeName)
	{
		return (Long) getIdValue(attributeName);
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return (Boolean) getIdValue(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return (Double) getIdValue(attributeName);
	}

	@Override
	public Instant getInstant(String attributeName)
	{
		return (Instant) getIdValue(attributeName);
	}

	@Override
	public LocalDate getLocalDate(String attributeName)
	{
		return (LocalDate) getIdValue(attributeName);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return (Entity) getIdValue(attributeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return (E) getIdValue(attributeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return (Iterable<Entity>) getIdValue(attributeName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return (Iterable<E>) getIdValue(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		setIdValue(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		values.getAttributeNames().forEach(attributeName -> setIdValue(attributeName, values.get(attributeName)));
	}

	private Object getIdValue(String attributeName)
	{
		Attribute idAttribute = entityType.getIdAttribute();
		if (attributeName.equals(idAttribute.getName()))
		{
			return id;
		}
		throw new UnsupportedOperationException(
				"Entity reference value cannot be retrieved except for the entity identifier");
	}

	private void setIdValue(String attributeName, Object id)
	{
		Attribute idAttribute = entityType.getIdAttribute();
		if (attributeName.equals(idAttribute.getName()))
		{
			this.id = validateIdType(requireNonNull(id));
		}
		else
		{
			throw new UnsupportedOperationException(
					"Entity reference values other than the identifier cannot be modified");
		}
	}

	private Object validateIdType(Object id)
	{
		Attribute idAttribute = entityType.getIdAttribute();
		switch (idAttribute.getDataType())
		{
			case EMAIL:
			case HYPERLINK:
			case STRING:
				if (!(id instanceof String))
				{
					throw new MolgenisDataException(
							format(ILLEGAL_VALUE_TYPE_MESSAGE, id.toString(), id.getClass().getSimpleName(),
									String.class.getSimpleName(), idAttribute.getName()));
				}
				break;
			case INT:
				if (!(id instanceof Integer))
				{
					throw new MolgenisDataException(
							format(ILLEGAL_VALUE_TYPE_MESSAGE, id.toString(), id.getClass().getSimpleName(),
									Integer.class.getSimpleName(), idAttribute.getName()));
				}
				break;
			case LONG:
				if (!(id instanceof Long))
				{
					throw new MolgenisDataException(
							format(ILLEGAL_VALUE_TYPE_MESSAGE, id.toString(), id.getClass().getSimpleName(),
									Long.class.getSimpleName(), idAttribute.getName()));
				}
				break;
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case ENUM:
			case FILE:
			case HTML:
			case MREF:
			case ONE_TO_MANY:
			case SCRIPT:
			case TEXT:
			case XREF:
				throw new MolgenisDataException(
						format("Illegal identifier value [%s] type [%s] for attribute [%s] with identifier type [%s]",
								id.toString(), id.getClass().getSimpleName(), idAttribute.getName(),
								idAttribute.getDataType().toString()));
			default:
				throw new UnexpectedEnumException(idAttribute.getDataType());
		}
		return id;
	}
}
