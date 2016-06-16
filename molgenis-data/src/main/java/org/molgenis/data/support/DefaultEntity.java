package org.molgenis.data.support;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static java.util.stream.StreamSupport.stream;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.EntityUtils;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class DefaultEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> values = new LinkedCaseInsensitiveMap<>();
	private final EntityMetaData entityMetaData;
	private transient final DataService dataService;

	// TODO remove dependency on DataService
	public DefaultEntity(EntityMetaData entityMetaData, DataService dataService, Map<String, Object> values)
	{
		this(entityMetaData, dataService);
		this.values.putAll(values);
	}

	// TODO remove dependency on DataService
	public DefaultEntity(EntityMetaData entityMetaData, DataService dataService, Entity entity)
	{
		this(entityMetaData, dataService);
		set(entity);
	}

	// TODO remove dependency on DataService
	public DefaultEntity(EntityMetaData entityMetaData, DataService dataService)
	{
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
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
	public void setIdValue(Object id)
	{
		AttributeMetaData idAttr = entityMetaData.getIdAttribute();
		if (idAttr == null)
		{
			throw new IllegalArgumentException(format("Entity [%s] doesn't have an id attribute"));
		}
		set(idAttr.getName(), id);
	}

	@Override
	public Object getLabelValue()
	{
		return getString(entityMetaData.getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null)
		{
			throw new UnknownAttributeException(attributeName);
		}

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
		return DataConverter.toString(values.get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(values.get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(values.get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(values.get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(values.get(attributeName));
	}

	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(values.get(attributeName));
	}

	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(values.get(attributeName));
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
		Object value = values.get(attributeName);
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
		Object value = values.get(attributeName);
		if (value == null) return null;
		if (value instanceof Entity) return (Entity) value;

		// value represents the id of the referenced entity
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		if (value instanceof Map)
			return new DefaultEntity(attribute.getRefEntity(), dataService, (Map<String, Object>) value);

		FieldType dataType = attribute.getDataType();
		if (!(dataType instanceof XrefField))
		{
			throw new MolgenisDataException(
					"can't use getEntity() on something that's not an xref, categorical or file");
		}

		value = dataType.convert(value);
		Entity refEntity = dataService.findOneById(attribute.getRefEntity().getName(), value);
		if (refEntity == null) throw new UnknownEntityException(
				attribute.getRefEntity().getName() + " with " + attribute.getRefEntity().getIdAttribute().getName()
						+ " [" + value + "] does not exist");

		return refEntity;
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME
		//		Entity entity = getEntity(attributeName);
		//		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity)).iterator()
		//				.next() : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		if (attribute == null) throw new UnknownAttributeException(attributeName);

		FieldType dataType = attribute.getDataType();

		// FIXME this should fail on anything other than instanceof MrefField. requires an extensive code base review to
		// find illegal use of getEntities()
		if (!(dataType instanceof MrefField) && !(dataType instanceof XrefField))
		{
			throw new MolgenisDataException(
					"can't use getEntities() on something that's not an xref, mref, categorical, categorical_mref or file");
		}

		Iterable<?> ids;

		Object value = values.get(attributeName);
		if (value instanceof String)
		{
			throw new RuntimeException("FIXME"); // FIXME
		}
		else if (value instanceof Entity) return Collections.singletonList((Entity) value);
		else ids = (Iterable<?>) value;

		if ((ids == null) || !ids.iterator().hasNext()) return Collections.emptyList();

		Object firstItem = ids.iterator().next();
		if (firstItem instanceof Entity) return (Iterable<Entity>) ids;

		if (firstItem instanceof Map)
		{
			return stream(ids.spliterator(), false)
					.map(id -> new DefaultEntity(attribute.getRefEntity(), dataService, (Map<String, Object>) id))
					.collect(Collectors.toList());
		}
		return from(ids).transform(dataType::convert)
				.transform(convertedId -> (dataService.findOneById(attribute.getRefEntity().getName(), convertedId)));
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME
		//		Iterable<Entity> entities = getEntities(attributeName);
		//		return entities != null ? new ConvertingIterable<E>(clazz, entities) : null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		values.put(attributeName, value);
	}

	@Override
	public void set(Entity entity)
	{
		entityMetaData.getAtomicAttributes().forEach(attr -> set(attr.getName(), entity.get(attr.getName())));
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Entity)) return false;
		return EntityUtils.equals(this, (Entity) o);
	}

	@Override
	public int hashCode()
	{
		return EntityUtils.hashCode(this);
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append(getEntityMetaData().getName()).append("{").append("values=").append(values)
				.append('}').toString();
	}
}
