package org.molgenis.data.support;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repo;
import org.molgenis.data.Repository;

public abstract class AbstractRepo extends AbstractRepository<Entity> implements Repo
{
	protected Repository<Entity> repository;
	
	@Override
	public Class<? extends Entity> getEntityClass()
	{
		return repository.getEntityClass();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return repository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		repository.close();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> entityClass)
	{
		return (Iterable<E>) new ConvertingIterable<E>(entityClass, repository);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return repository.getEntityMetaData();
	}

	public AbstractRepo()
	{
		super();
	}

}