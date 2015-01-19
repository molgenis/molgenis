package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

public abstract class AbstractRepository implements Repository
{
	private String name;

	@Override
	public String getName()
	{
		if (name == null) name = getEntityMetaData().getName();
		return name;
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return new ConvertingIterable<E>(clazz, this);
	}
}
