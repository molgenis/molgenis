package org.molgenis.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.framework.db.Database.DatabaseAction;

/**
 * Base class for CrudRepository decorators.
 * 
 * In subclass override the methods you want to decorate
 */
public class CrudRepositoryDecorator<E extends Entity> implements CrudRepository<E>
{
	private final CrudRepository<E> decoratedRepository;

	public CrudRepositoryDecorator(CrudRepository<E> decoratedRepository)
	{
		if (decoratedRepository == null) throw new IllegalArgumentException("decoratedRepository is null");
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public void add(E entity)
	{
		decoratedRepository.add(entity);
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public void update(E entity)
	{
		decoratedRepository.update(entity);
	}

	@Override
	public void add(Iterable<E> entities)
	{
		decoratedRepository.add(entities);
	}

	@Override
	public void update(Iterable<E> records)
	{
		decoratedRepository.update(records);
	}

	@Override
	public Iterable<E> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
	}

	@Override
	public String getName()
	{
		return decoratedRepository.getName();
	}

	@Override
	public void flush()
	{
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepository.clearCache();
	}

	@Override
	public void delete(E entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public E findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public String getLabel()
	{
		return decoratedRepository.getLabel();
	}

	@Override
	public void delete(Iterable<E> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public String getDescription()
	{
		return decoratedRepository.getDescription();
	}

	@Override
	public void deleteById(Integer id)
	{
		decoratedRepository.deleteById(id);
	}

	@Override
	public Iterator<E> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public E findOne(Integer id)
	{
		return decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<AttributeMetaData> getAttributes()
	{
		return decoratedRepository.getAttributes();
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		decoratedRepository.deleteById(ids);
	}

	@Override
	public Iterable<E> findAll(Iterable<Integer> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public void update(List<E> entities, DatabaseAction dbAction, String... keyName)
	{
		decoratedRepository.update(entities, dbAction, keyName);
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

}
