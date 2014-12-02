package org.molgenis.data.support;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.util.MolgenisDateFormat;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Wraps an entity that behaves like an entity with other entity meta data. Reference data types (e.g. categorical,
 * xref, mref) in the other entity meta data are dealt with as follows: - source value is a entity (collection): use
 * source value - source value is not a entity (collection): assume source value is a entity id (collection)
 */
public class TransformedEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final Entity entity;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	public TransformedEntity(Entity entity, EntityMetaData entityMetaData, DataService dataService)
	{
		if (entity == null) throw new IllegalArgumentException("entity is null");
		if (entityMetaData == null) throw new IllegalArgumentException("entityMetaData is null");
		if (dataService == null) throw new IllegalArgumentException("dataService is null");
		this.entity = entity;
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
		return get(entityMetaData.getIdAttribute().getName());
	}

	@Override
	public String getLabelValue()
	{
		return entity.getString(entityMetaData.getLabelAttribute().getName());
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
		Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new java.sql.Date(utilDate.getTime()) : null;
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		Object value = entity.get(attributeName);
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
		Date utilDate = getUtilDate(attributeName);
		return utilDate != null ? new Timestamp(utilDate.getTime()) : null;
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		Object value = entity.get(attributeName);
		if (value == null)
		{
			return null;
		}
		if (value instanceof Entity)
		{
			return (Entity) value;
		}
		else
		{
			// value represents the id of the referenced entity
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);
			EntityMetaData refEntityMetaData = attribute.getRefEntity();
			return new LazyReferenceEntity(value, refEntityMetaData, dataService);
		}
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		Entity entity = getEntity(attributeName);
		return entity != null ? new ConvertingIterable<E>(clazz, Arrays.asList(entity)).iterator().next() : null;
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		List<String> list = getList(attributeName);
		if (list != null)
		{
			AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
			if (attribute == null) throw new UnknownAttributeException(attributeName);
			final EntityMetaData refEntityMetaData = attribute.getRefEntity();
			return Iterables.transform(list, new Function<String, Entity>()
			{
				@Override
				public Entity apply(String id)
				{
					// value represents the id of the referenced entity
					return new LazyReferenceEntity(id, refEntityMetaData, dataService);
				}
			});
		}
		return null;
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		Iterable<Entity> entities = getEntities(attributeName);
		return entities != null ? new ConvertingIterable<E>(clazz, entities) : null;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is not mutable");
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is not mutable");
	}

	@Override
	public void set(Entity values)
	{
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is not mutable");
	}
}
