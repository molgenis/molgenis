package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.MolgenisDateFormat;

/**
 * {@link Entity} decorator that uses an entity with different {@link EntityMetaData}. Values are converted on demand to
 * the data types defined in the new EntityMetaData.
 */
public class EntityDecorator implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Entity entity;
	private final EntityMetaData entityMetaData;
	private transient final DataService dataService;

	public EntityDecorator(Entity entity, EntityMetaData entityMetaData, DataService dataService)
	{
		this.entity = requireNonNull(entity);
		this.entityMetaData = requireNonNull(entityMetaData);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return EntityMetaDataUtils.getAttributeNames(entityMetaData.getAtomicAttributes());
	}

	@Override
	public Object getIdValue()
	{
		return get(entityMetaData.getIdAttribute().getName());
	}

	@Override
	public String getLabelValue()
	{
		return getString(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		FieldTypeEnum dataType = attribute.getDataType().getEnumType();
		switch (dataType)
		{
			case BOOL:
				return getBoolean(attributeName);
			case CATEGORICAL:
			case XREF:
			case FILE:
				return getEntity(attributeName);
			case COMPOUND:
				throw new UnsupportedOperationException();
			case DATE:
				return getDate(attributeName);
			case DATE_TIME:
				return getUtilDate(attributeName);
			case DECIMAL:
				return getDouble(attributeName);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return getString(attributeName);
			case IMAGE:
				throw new MolgenisDataException("Unsupported data type [" + dataType + "]");
			case INT:
				return getInt(attributeName);
			case LONG:
				return getLong(attributeName);
			case CATEGORICAL_MREF:
			case MREF:
				return getEntities(attributeName);
			default:
				throw new RuntimeException("Unknown data type [" + dataType + "]");
		}
	}

	@Override
	public String getString(String attributeName)
	{
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute != null)
		{
			FieldType dataType = attribute.getDataType();
			if (dataType instanceof XrefField)
			{
				return DataConverter.toString(getEntity(attributeName));
			}
		}
		return DataConverter.toString(entity.get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(entity.get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(entity.get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(entity.get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(entity.get(attributeName));
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(entity.get(attributeName));
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(entity.get(attributeName));
	}

	@Override
	public java.sql.Date getDate(String attributeName)
	{
		java.util.Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		Object value = entity.get(attributeName);
		if (value == null) return null;
		if (value instanceof java.util.Date) return (java.util.Date) value;

		try
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);

			FieldTypeEnum dataType = attribute.getDataType().getEnumType();
			switch (dataType)
			{
				case DATE:
					return MolgenisDateFormat.getDateFormat().parse(value.toString());
				case DATE_TIME:
					return MolgenisDateFormat.getDateTimeFormat().parse(value.toString());
				// $CASES-OMITTED$
				default:
					throw new MolgenisDataException("Type [" + dataType + "] is not a date type");

			}
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException(e);
		}
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		java.util.Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new Timestamp(utilDate.getTime()) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Entity getEntity(String attributeName)
	{
		Object value = entity.get(attributeName);
		if (value == null) return null;
		if (value instanceof Entity) return (Entity) value;

		// value represents the id of the referenced entity
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		if (value instanceof Map)
			return new DefaultEntity(attribute.getRefEntity(), dataService, (Map<String, Object>) value);

		FieldType dataType = attribute.getDataType();
		if (attribute.getDataType().equals(MolgenisFieldTypes.MREF)
				|| attribute.getDataType().equals(MolgenisFieldTypes.CATEGORICAL_MREF))
		{
			throw new MolgenisDataException(
					"can't use getEntity() on an mref/categorical_mref, use getEntities() instead");
		}

		value = dataType.convert(value);
		return new LazyEntity(attribute.getRefEntity(), dataService, value);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		Entity entity = getEntity(attributeName);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity), dataService).iterator().next()
				: null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		Iterable<?> ids;
		Object value = entity.get(attributeName);
		if (value instanceof String) ids = getList(attributeName);
		else if (value instanceof Entity) return Collections.singletonList((Entity) value);
		else ids = (Iterable<?>) value;

		if ((ids == null) || !ids.iterator().hasNext()) return Collections.emptyList();
		if (ids.iterator().next() instanceof Entity) return (Iterable<Entity>) ids;

		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		EntityMetaData refEntityMeta = attribute.getRefEntity();
		if (ids.iterator().next() instanceof Map)
		{
			return stream(ids.spliterator(), false)
					.map(id -> new DefaultEntity(refEntityMeta, dataService, (Map<String, Object>) id))
					.collect(Collectors.toList());
		}

		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return stream(ids.spliterator(), false).map(id -> {
					Object convertedId = attribute.getDataType().convert(id);
					Entity refEntity = new LazyEntity(refEntityMeta, dataService, convertedId);
					return refEntity;
				}).iterator();
			}
		};
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		Iterable<Entity> entities = getEntities(attributeName);
		return entities != null ? new ConvertingIterable<E>(clazz, entities, dataService) : null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		entity.set(attributeName, value);
	}

	@Override
	public void set(Entity entity)
	{
		entityMetaData.getAtomicAttributes().forEach(attr -> set(attr.getName(), entity.get(attr.getName())));
	}

	@Override
	public String toString()
	{
		return getLabelValue();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityMetaData == null) ? 0 : entityMetaData.hashCode());
		result = prime * result + ((getIdValue() == null) ? 0 : getIdValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Entity)) return false;
		Entity other = (Entity) obj;

		if (entityMetaData == null)
		{
			if (other.getEntityMetaData() != null) return false;
		}
		else if (!entityMetaData.equals(other.getEntityMetaData())) return false;
		if (getIdValue() == null)
		{
			if (other.getIdValue() != null) return false;
		}
		else if (!getIdValue().equals(other.getIdValue())) return false;
		return true;
	}
}
