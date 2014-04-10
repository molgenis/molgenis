package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;

public abstract class AbstractRepository implements Repository
{
	private final String url;
	private String name;

	public AbstractRepository(String url)
	{
		this.url = url;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

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
