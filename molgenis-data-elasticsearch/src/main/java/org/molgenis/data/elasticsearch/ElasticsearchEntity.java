package org.molgenis.data.elasticsearch;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.support.ConvertingIterable;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.util.MolgenisDateFormat;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class ElasticsearchEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> source;
	private final EntityMetaData entityMetaData;

	public ElasticsearchEntity(Map<String, Object> source, EntityMetaData entityMetaData)
	{
		if (source == null) throw new IllegalArgumentException("source is null");
		if (entityMetaData == null) throw new IllegalArgumentException("entityMetaData is null");
		this.source = source;
		this.entityMetaData = entityMetaData;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		// TODO code duplication with TransformedEntity
		// TODO create utility method to get all attributes (atomics + compounds) and use this method here

		// atomic
		Iterable<String> atomicAttributes = Iterables.transform(entityMetaData.getAtomicAttributes(),
				new Function<AttributeMetaData, String>()
				{

					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				});

		// compound
		Iterable<String> compoundAttributes = Iterables.transform(
				Iterables.filter(entityMetaData.getAttributes(), new Predicate<AttributeMetaData>()
				{
					@Override
					public boolean apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getDataType().getEnumType() == FieldTypeEnum.COMPOUND;
					}
				}), new Function<AttributeMetaData, String>()
				{
					@Override
					public String apply(AttributeMetaData attributeMetaData)
					{
						return attributeMetaData.getName();
					}
				});

		// all = atomic + compound
		return Iterables.concat(atomicAttributes, compoundAttributes);
	}

	@Override
	public Object getIdValue()
	{
		return source.get(getEntityMetaData().getIdAttribute().getName());
	}

	@Override
	public String getLabelValue()
	{
		return source.get(entityMetaData.getLabelAttribute().getName()).toString();
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Collections.singletonList(entityMetaData.getLabelAttribute().getName());
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
				return getEntity(attributeName);
			case COMPOUND:
				throw new UnsupportedOperationException();
			case DATE:
				return getDate(attributeName);
			case DATE_TIME:
				return getDate(attributeName);
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
			case FILE:
			case IMAGE:
				throw new MolgenisDataException("Unsupported data type [" + dataType + "]");
			case INT:
				return getInt(attributeName);
			case LONG:
				return getLong(attributeName);
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
			if (attribute.getDataType() instanceof XrefField)
			{
				return getEntity(attributeName).getLabelValue();
			}

			if (attribute.getDataType() instanceof MrefField)
			{
				return Joiner.on(",").join(
						Iterables.transform(getEntities(attributeName), new Function<Entity, String>()
						{
							@Override
							public String apply(Entity entity)
							{
								return entity.getLabelValue();
							}
						}));
			}
		}

		return DataConverter.toString(source.get(attributeName));
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return DataConverter.toInt(source.get(attributeName));
	}

	@Override
	public Long getLong(String attributeName)
	{
		return DataConverter.toLong(source.get(attributeName));
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return DataConverter.toBoolean(source.get(attributeName));
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return DataConverter.toDouble(source.get(attributeName));
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
		Object value = source.get(attributeName);
		if (value == null) return null;

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

	@Override
	public abstract Entity getEntity(String attributeName);

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		Entity entity = getEntity(attributeName);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity)).iterator().next() : null;
	}

	@Override
	public abstract Iterable<Entity> getEntities(String attributeName);

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		Iterable<Entity> entities = getEntities(attributeName);
		return entities != null ? new ConvertingIterable<E>(clazz, entities) : null;
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return DataConverter.toList(source.get(attributeName));
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return DataConverter.toIntList(source.get(attributeName));
	}

	@Override
	public abstract void set(String attributeName, Object value);

	@Override
	public void set(Entity values)
	{
		throw new UnsupportedOperationException("Immutable entity");
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException("Immutable entity");
	}

	protected Map<String, Object> getSource()
	{
		return source;
	}
}
