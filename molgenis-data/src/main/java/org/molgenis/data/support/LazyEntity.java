package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;
import static java.util.stream.StreamSupport.stream;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;

public class LazyEntity implements Entity
{
	private static final long serialVersionUID = 1L;

	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	private final Object id;

	private Entity entity;

	public LazyEntity(EntityMetaData entityMetaData, DataService dataService, Object id)
	{
		this.entityMetaData = requireNonNull(entityMetaData);
		this.dataService = requireNonNull(dataService);
		this.id = requireNonNull(id);
	}

	@Override
	public Object getIdValue()
	{
		return id;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		// FIXME code duplication with DefaultEntity
		return new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				Stream<String> atomic = stream(entityMetaData.getAtomicAttributes().spliterator(), false)
						.map(a -> a.getName());
				Stream<String> compound = stream(entityMetaData.getAttributes().spliterator(), false)
						.filter(a -> a.getDataType().getEnumType() == FieldTypeEnum.COMPOUND).map(a -> a.getName());

				return Stream.concat(atomic, compound).iterator();
			}
		};
	}

	@Override
	public String getLabelValue()
	{
		return getLazyLoadedEntity().getLabelValue();

	}

	@Override
	public Object get(String attributeName)
	{
		return getLazyLoadedEntity().get(attributeName);
	}

	@Override
	public String getString(String attributeName)
	{
		return getLazyLoadedEntity().getString(attributeName);
	}

	@Override
	public Integer getInt(String attributeName)
	{
		return getLazyLoadedEntity().getInt(attributeName);
	}

	@Override
	public Long getLong(String attributeName)
	{
		return getLazyLoadedEntity().getLong(attributeName);
	}

	@Override
	public Boolean getBoolean(String attributeName)
	{
		return getLazyLoadedEntity().getBoolean(attributeName);
	}

	@Override
	public Double getDouble(String attributeName)
	{
		return getLazyLoadedEntity().getDouble(attributeName);
	}

	@Override
	public Date getDate(String attributeName)
	{
		return getLazyLoadedEntity().getDate(attributeName);
	}

	@Override
	public java.util.Date getUtilDate(String attributeName)
	{
		return getLazyLoadedEntity().getUtilDate(attributeName);
	}

	@Override
	public Timestamp getTimestamp(String attributeName)
	{
		return getLazyLoadedEntity().getTimestamp(attributeName);
	}

	@Override
	public Entity getEntity(String attributeName)
	{
		return getLazyLoadedEntity().getEntity(attributeName);
	}

	@Override
	public <E extends Entity> E getEntity(String attributeName, Class<E> clazz)
	{
		return getLazyLoadedEntity().getEntity(attributeName, clazz);
	}

	@Override
	public Iterable<Entity> getEntities(String attributeName)
	{
		return getLazyLoadedEntity().getEntities(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> getEntities(String attributeName, Class<E> clazz)
	{
		return getLazyLoadedEntity().getEntities(attributeName, clazz);
	}

	@Override
	public List<String> getList(String attributeName)
	{
		return getLazyLoadedEntity().getList(attributeName);
	}

	@Override
	public List<Integer> getIntList(String attributeName)
	{
		return getLazyLoadedEntity().getIntList(attributeName);
	}

	@Override
	public void set(String attributeName, Object value)
	{
		getLazyLoadedEntity().set(attributeName, value);
	}

	@Override
	public void set(Entity values)
	{
		getLazyLoadedEntity().set(values);
	}

	private Entity getLazyLoadedEntity()
	{
		if (entity == null)
		{
			entity = dataService.findOne(getEntityMetaData().getName(), id);
			if (entity == null)
			{
				throw new UnknownEntityException(
						getEntityMetaData().getName() + " with " + getEntityMetaData().getIdAttribute().getName() + " ["
								+ getIdValue().toString() + "] does not exist");
			}
		}
		return entity;
	}
}
