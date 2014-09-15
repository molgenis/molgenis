package org.molgenis.data;

import java.io.IOException;

import org.molgenis.data.support.QueryImpl;

/**
 * Base class for CrudRepository decorators.
 * 
 * In subclass override the methods you want to decorate
 */
public class CrudRepositoryDecorator extends RepositoryDecorator implements CrudRepository
{
	private final CrudRepository decoratedRepository;

	public CrudRepositoryDecorator(CrudRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public long count()
	{
		return decoratedRepository.count();
	}

	@Override
	public void add(Entity entity)
	{
		decoratedRepository.add(entity);
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		return decoratedRepository.count(q);
	}

	@Override
	public void update(Entity entity)
	{
		decoratedRepository.update(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		return decoratedRepository.add(entities);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		decoratedRepository.update(records);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		return decoratedRepository.findAll(q);
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
	public void delete(Entity entity)
	{
		decoratedRepository.delete(entity);
	}

	@Override
	public Entity findOne(Query q)
	{
		return decoratedRepository.findOne(q);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		decoratedRepository.delete(entities);
	}

	@Override
	public Entity findOne(Object id)
	{
		return decoratedRepository.findOne(id);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepository.close();
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		decoratedRepository.deleteById(ids);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return decoratedRepository.findAll(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		return decoratedRepository.findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		return decoratedRepository.findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		return decoratedRepository.findOne(q, clazz);
	}

	@Override
	public void deleteById(Object id)
	{
		decoratedRepository.deleteById(id);
	}

	protected CrudRepository getDecoratedRepository()
	{
		return decoratedRepository;
	}
}
