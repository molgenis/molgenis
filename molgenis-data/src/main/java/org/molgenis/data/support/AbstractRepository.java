package org.molgenis.data.support;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;

public abstract class AbstractRepository implements Repository
{

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

	@Override
	public String getLabel()
	{
		return getEntityMetaData().getLabel();
	}

	@Override
	public String getDescription()
	{
		return getEntityMetaData().getDescription();
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return getEntityMetaData().getAttributes();
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		return getEntityMetaData().getIdAttribute();
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return getEntityMetaData().getLabelAttribute();
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		return getEntityMetaData().getAttribute(attributeName);
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return (Iterable<E>) new ConvertingIterable<E>(clazz, this);
	}

	protected abstract EntityMetaData getEntityMetaData();

}
