package org.molgenis.data;

import java.io.IOException;
import java.util.Iterator;

public class RepositoryDecorator implements Repository
{
	private final Repository decoratedRepository;

	public RepositoryDecorator(Repository decoratedRepository)
	{
		if (decoratedRepository == null) throw new IllegalArgumentException("decoratedRepsitory is null");
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepository.getEntityMetaData();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		return decoratedRepository.iterator(clazz);
	}

	@Override
	public String getUrl()
	{
		return decoratedRepository.getUrl();
	}
}
