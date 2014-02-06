package org.molgenis.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for CrudRepository decorators.
 * 
 * In subclass override the methods you want to decorate
 */
public class CrudRepositoryDecorator implements CrudRepository
{
	private final CrudRepository decoratedRepository;

	public CrudRepositoryDecorator(CrudRepository decoratedRepository)
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
	public Integer add(Entity entity)
	{
		return decoratedRepository.add(entity);
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
	public void add(Iterable<? extends Entity> entities)
	{
		decoratedRepository.add(entities);
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
	public String getLabel()
	{
		return decoratedRepository.getLabel();
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
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
	public Iterator<Entity> iterator()
	{
		return decoratedRepository.iterator();
	}

	@Override
	public Entity findOne(Integer id)
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
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		return decoratedRepository.findAll(ids);
	}

	@Override
	public void deleteAll()
	{
		decoratedRepository.deleteAll();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
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
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		return decoratedRepository.findAll(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		return decoratedRepository.findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		return decoratedRepository.findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		return decoratedRepository.findOne(q, clazz);
	}

	@Override
	public String getUrl()
	{
		return decoratedRepository.getUrl();
	}
}
