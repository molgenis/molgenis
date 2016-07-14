package org.molgenis.annotation.cmd;

import org.molgenis.data.*;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by charbonb on 12/07/16.
 */
public class CmdLineDataService implements DataService
{
	@Override
	public void setMetaDataService(MetaDataService metaDataService)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public MetaDataService getMeta()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities(String repositoryName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		return false;
	}

	@Override
	public Repository<Entity> getRepository(String entityName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityMetaData getEntityMetaData(String entityName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count(String entityName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count(String entityName, Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityName, Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Query<E> q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityName, Stream<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityName, Stream<Object> ids, Fetch fetch)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Fetch fetch, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOneById(String entityName, Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOneById(String entityName, Object id, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOneById(String entityName, Object id, Fetch fetch)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOneById(String entityName, Object id, Fetch fetch, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(String entityName, Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Query<E> q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(String entityName, Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> void add(String entityName, Stream<E> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(String entityName, Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> void update(String entityName, Stream<E> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String entityName, Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> void delete(String entityName, Stream<E> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(String entityName, Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll(String entityName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Query<Entity> query(String entityName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Query<E> query(String entityName, Class<E> entityClass)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AggregateResult aggregate(String entityName, AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<String> getEntityNames()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addEntityListener(String entityName, EntityListener entityListener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEntityListener(String entityName, EntityListener entityListener)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Repository<Entity> copyRepository(Repository<Entity> repository, String newRepositoryId,
			String newRepositoryLabel)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Repository<Entity> copyRepository(Repository<Entity> repository, String newRepositoryId,
			String newRepositoryLabel, Query<Entity> query)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		throw new UnsupportedOperationException();
	}
}
