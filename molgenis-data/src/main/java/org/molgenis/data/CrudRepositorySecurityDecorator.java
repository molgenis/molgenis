package org.molgenis.data;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;

public class CrudRepositorySecurityDecorator extends CrudRepositoryDecorator
{
	private final CrudRepository decoratedRepository;

	public CrudRepositorySecurityDecorator(CrudRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.close();
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
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.iterator(clazz);
	}

	@Override
	public String getUrl()
	{
		return decoratedRepository.getUrl();
	}

	@Override
	public Query query()
	{
		return new QueryImpl(this);
	}

	@Override
	public long count(Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(q, clazz);
	}

	@Override
	public Entity findOne(Query q)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Object id)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(ids);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Object> ids, Class<E> clazz)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Object id, Class<E> clazz)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		validatePermission(decoratedRepository.getName(), Permission.READ);
		return decoratedRepository.findOne(q, clazz);
	}

	@Override
	public long count()
	{
		validatePermission(decoratedRepository.getName(), Permission.COUNT);
		return decoratedRepository.count();
	}

	@Override
	public void update(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.deleteAll();
	}

	@Override
	public void add(Entity entity)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		return decoratedRepository.add(entities);
	}

	@Override
	public void flush()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		validatePermission(decoratedRepository.getName(), Permission.WRITE);
		decoratedRepository.clearCache();
	}

}
