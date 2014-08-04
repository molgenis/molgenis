package org.molgenis.data.mongodb;

import static org.molgenis.util.SecurityDecoratorUtils.validatePermission;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.security.core.Permission;

public class MongoRepositorySecurityDecorator implements MongoRepository
{
	private final MongoRepository mongoRepository;

	public MongoRepositorySecurityDecorator(MongoRepository mongoRepository)
	{
		this.mongoRepository = mongoRepository;
	}

	@Override
	public void close() throws IOException
	{
		mongoRepository.close();
	}

	@Override
	public long count()
	{
		validatePermission(mongoRepository.getName(), Permission.COUNT);
		return mongoRepository.count();
	}

	@Override
	public void update(Entity entity)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> entities)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.deleteAll();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.update(entities, dbAction, keyName);

	}

	@Override
	public void add(Entity entity)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.add(entity);
	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		return mongoRepository.add(entities);
	}

	@Override
	public void flush()
	{
		validatePermission(mongoRepository.getName(), Permission.WRITE);
		mongoRepository.flush();
	}

	@Override
	public void clearCache()
	{
		mongoRepository.clearCache();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		validatePermission(mongoRepository.getName(), Permission.READ);
		return mongoRepository.iterator();
	}

	@Override
	public String getName()
	{
		return mongoRepository.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return mongoRepository.getEntityMetaData();
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		validatePermission(mongoRepository.getName(), Permission.READ);
		return mongoRepository.iterator(clazz);
	}

	@Override
	public String getUrl()
	{
		return mongoRepository.getUrl();
	}

	@Override
	public Entity findOne(Object id)
	{
		validatePermission(mongoRepository.getName(), Permission.READ);
		return mongoRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<?> ids)
	{
		validatePermission(mongoRepository.getName(), Permission.READ);
		return mongoRepository.findAll(ids);
	}

}
