package org.molgenis.annotation.cmd.data;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

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
	public boolean hasRepository(String entityTypeId)
	{
		return false;
	}

	@Override
	public Repository<Entity> getRepository(String entityTypeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Repository<E> getRepository(String entityTypeId, Class<E> entityClass)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityType getEntityType(String entityTypeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count(String entityTypeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long count(String entityTypeId, Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId, Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Query<E> q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId, Stream<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Stream<Object> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId, Stream<Object> ids, Fetch fetch)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Stream<Object> ids, Fetch fetch, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOneById(String entityTypeId, Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOneById(String entityTypeId, Object id, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOneById(String entityTypeId, Object id, Fetch fetch)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOneById(String entityTypeId, Object id, Fetch fetch, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(String entityTypeId, Query<Entity> q)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(String entityTypeId, Query<E> q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(String entityTypeId, Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> void add(String entityTypeId, Stream<E> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(String entityTypeId, Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> void update(String entityTypeId, Stream<E> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String entityTypeId, Entity entity)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> void delete(String entityTypeId, Stream<E> entities)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteById(String entityTypeId, Object id)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll(String entityTypeId, Stream<Object> ids)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteAll(String entityTypeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Query<Entity> query(String entityTypeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> Query<E> query(String entityTypeId, Class<E> entityClass)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AggregateResult aggregate(String entityTypeId, AggregateQuery aggregateQuery)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<String> getEntityTypeIds()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		throw new UnsupportedOperationException();
	}
}
