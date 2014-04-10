package org.molgenis.data;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.security.core.Permission;

public class CrudRepositorySecurityDecorator extends CrudRepositoryDecorator implements CrudRepository
{
	private final CrudRepository decoratedRepository;
	private final String entityName;

	public CrudRepositorySecurityDecorator(CrudRepository decoratedRepository)
	{
		super(decoratedRepository);
		this.decoratedRepository = decoratedRepository;
		this.entityName = decoratedRepository.getName();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		validatePermission(Permission.READ);
		return decoratedRepository.iterator();
	}

	@Override
	public void close() throws IOException
	{
		validatePermission(Permission.WRITE);
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
		validatePermission(Permission.READ);
		return decoratedRepository.iterator(clazz);
	}

	@Override
	public String getUrl()
	{
		return decoratedRepository.getUrl();
	}

	@Override
	public long count(Query q)
	{
		validatePermission(Permission.COUNT);
		return decoratedRepository.count(q);
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findAll(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findAll(q, clazz);
	}

	@Override
	public Entity findOne(Query q)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findOne(q);
	}

	@Override
	public Entity findOne(Integer id)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findOne(id);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findAll(ids);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		validatePermission(Permission.READ);
		return decoratedRepository.findOne(q, clazz);
	}

	@Override
	public long count()
	{
		validatePermission(Permission.COUNT);
		return decoratedRepository.count();
	}

	@Override
	public void update(Entity entity)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.update(entity);
	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.update(records);
	}

	@Override
	public void delete(Entity entity)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.delete(entities);
	}

	@Override
	public void deleteById(Integer id)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.deleteById(id);
	}

	@Override
	public void deleteById(Iterable<Integer> ids)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.deleteById(ids);
	}

	@Override
	public void deleteAll()
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.deleteAll();
	}

	@Override
	public void update(List<? extends Entity> entities, DatabaseAction dbAction, String... keyName)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.update(entities, dbAction, keyName);
	}

	@Override
	public Integer add(Entity entity)
	{
		validatePermission(Permission.WRITE);
		return decoratedRepository.add(entity);
	}

	@Override
	public void add(Iterable<? extends Entity> entities)
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.add(entities);
	}

	@Override
	public void flush()
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.flush();
	}

	@Override
	public void clearCache()
	{
		validatePermission(Permission.WRITE);
		decoratedRepository.clearCache();
	}

	protected void validatePermission(Permission permission)
	{
		String role = String.format("ROLE_ENTITY_%s_%s", permission.toString(), entityName.toUpperCase());
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", role))
		{
			throw new MolgenisDataAccessException("No " + permission.toString() + " permission on entity " + entityName);
		}
	}
}
