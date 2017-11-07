package org.molgenis.data.support;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of the DataService interface
 */
@Component
public class DataServiceImpl implements DataService
{
	private MetaDataService metaDataService;

	public void setMetaDataService(MetaDataService metaDataService)
	{
		this.metaDataService = requireNonNull(metaDataService);
	}

	@Override
	public EntityType getEntityType(String entityTypeId)
	{
		return metaDataService.getEntityType(entityTypeId);
	}

	@Override
	public synchronized Stream<String> getEntityTypeIds()
	{
		return metaDataService.getEntityTypes().map(EntityType::getId);
	}

	@Override
	public boolean hasRepository(String entityTypeId)
	{
		return metaDataService.hasRepository(entityTypeId);
	}

	@Override
	public long count(String entityTypeId)
	{
		return getRepository(entityTypeId).count();
	}

	@Override
	public long count(String entityTypeId, Query<Entity> q)
	{
		return getRepository(entityTypeId).count(q);
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId)
	{
		return findAll(entityTypeId, query(entityTypeId));
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId, Query<Entity> q)
	{
		return getRepository(entityTypeId).findAll(q);
	}

	@Override
	public Entity findOneById(String entityTypeId, Object id)
	{
		return getRepository(entityTypeId).findOneById(id);
	}

	@Override
	public Entity findOne(String entityTypeId, Query<Entity> q)
	{
		return getRepository(entityTypeId).findOne(q);
	}

	@Override
	@Transactional
	public void add(String entityTypeId, Entity entity)
	{
		getRepository(entityTypeId).add(entity);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Entity> void add(String entityTypeId, Stream<E> entities)
	{
		getRepository(entityTypeId).add((Stream<Entity>) entities);
	}

	@Override
	@Transactional
	public void update(String entityTypeId, Entity entity)
	{
		getRepository(entityTypeId).update(entity);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Entity> void update(String entityTypeId, Stream<E> entities)
	{
		getRepository(entityTypeId).update((Stream<Entity>) entities);
	}

	@Override
	@Transactional
	public void delete(String entityTypeId, Entity entity)
	{
		getRepository(entityTypeId).delete(entity);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Entity> void delete(String entityTypeId, Stream<E> entities)
	{
		getRepository(entityTypeId).delete((Stream<Entity>) entities);
	}

	@Override
	@Transactional
	public void deleteById(String entityTypeId, Object id)
	{
		getRepository(entityTypeId).deleteById(id);
	}

	@Override
	@Transactional
	public void deleteAll(String entityTypeId, Stream<Object> ids)
	{
		getRepository(entityTypeId).deleteAll(ids);
	}

	@Override
	@Transactional
	public void deleteAll(String entityTypeId)
	{
		getRepository(entityTypeId).deleteAll();
	}

	@Override
	public Repository<Entity> getRepository(String entityTypeId)
	{
		return metaDataService.getRepository(entityTypeId);
	}

	@SuppressWarnings("unchecked")
	public <E extends Entity> Repository<E> getRepository(String entityTypeId, Class<E> entityClass)
	{
		return (Repository<E>) getRepository(entityTypeId);
	}

	@Override
	public Query<Entity> query(String entityTypeId)
	{
		return new QueryImpl<>(getRepository(entityTypeId));
	}

	@Override
	public <E extends Entity> Query<E> query(String entityTypeId, Class<E> entityClass)
	{
		return new QueryImpl<>(getRepository(entityTypeId, entityClass));
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Query<E> q, Class<E> clazz)
	{
		return getRepository(entityTypeId, clazz).findAll(q);
	}

	@Override
	public <E extends Entity> E findOneById(String entityTypeId, Object id, Class<E> clazz)
	{
		return getRepository(entityTypeId, clazz).findOneById(id);
	}

	@Override
	public <E extends Entity> E findOne(String entityTypeId, Query<E> q, Class<E> clazz)
	{
		return getRepository(entityTypeId, clazz).findOne(q);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Class<E> clazz)
	{
		return findAll(entityTypeId, query(entityTypeId, clazz), clazz);
	}

	@Override
	public AggregateResult aggregate(String entityTypeId, AggregateQuery aggregateQuery)
	{
		return getRepository(entityTypeId).aggregate(aggregateQuery);
	}

	@Override
	public MetaDataService getMeta()
	{
		return metaDataService;
	}

	@Override
	public synchronized Iterator<Repository<Entity>> iterator()
	{
		return metaDataService.getRepositories().iterator();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities(String repositoryName)
	{
		return getRepository(repositoryName).getCapabilities();
	}

	@Override
	public Entity findOneById(String entityTypeId, Object id, Fetch fetch)
	{
		return getRepository(entityTypeId).findOneById(id, fetch);
	}

	@Override
	public <E extends Entity> E findOneById(String entityTypeId, Object id, Fetch fetch, Class<E> clazz)
	{
		return getRepository(entityTypeId, clazz).findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId, Stream<Object> ids)
	{
		return getRepository(entityTypeId).findAll(ids);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Stream<Object> ids, Class<E> clazz)
	{
		return getRepository(entityTypeId, clazz).findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(String entityTypeId, Stream<Object> ids, Fetch fetch)
	{
		return getRepository(entityTypeId).findAll(ids, fetch);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityTypeId, Stream<Object> ids, Fetch fetch, Class<E> clazz)
	{
		return getRepository(entityTypeId, clazz).findAll(ids, fetch);
	}
}
