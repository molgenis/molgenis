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
	public String getLabel()
	{
		return decoratedRepository.getLabel();
	}

	@Override
	public String getDescription()
	{
		return decoratedRepository.getDescription();
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return decoratedRepository.getAttributes();
	}

	@Override
	public AttributeMetaData getIdAttribute()
	{
		return decoratedRepository.getIdAttribute();
	}

	@Override
	public AttributeMetaData getLabelAttribute()
	{
		return decoratedRepository.getLabelAttribute();
	}

	@Override
	public AttributeMetaData getAttribute(String attributeName)
	{
		return decoratedRepository.getAttribute(attributeName);
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
	public Class<? extends Entity> getEntityClass()
	{
		return decoratedRepository.getEntityClass();
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

	@Override
	public Iterable<AttributeMetaData> getLevelOneAttributes()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
