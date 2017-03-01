package org.molgenis.data.support;

import org.molgenis.data.*;
import org.molgenis.data.aggregation.AggregateQuery;
import org.molgenis.data.aggregation.AggregateResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

/**
 * Implementation of the DataService interface
 */
@Component
public class DataServiceImpl implements DataService
{
	private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

	private MetaDataService metaDataService;

	public void setMetaDataService(MetaDataService metaDataService)
	{
		this.metaDataService = requireNonNull(metaDataService);
	}

	@Override
	public EntityType getEntityType(String entityName)
	{
		return metaDataService.getEntityType(entityName);
	}

	@Override
	public synchronized Stream<String> getEntityNames()
	{
		return metaDataService.getEntityTypes().map(EntityType::getFullyQualifiedName);
	}

	@Override
	public Stream<Object> getEntityIds()
	{
		return metaDataService.getEntityTypes().map(EntityType::getId);
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		return metaDataService.hasRepository(entityName);
	}

	@Override
	public long count(String entityName)
	{
		return getRepository(entityName).count();
	}

	@Override
	public long count(String entityName, Query<Entity> q)
	{
		return getRepository(entityName).count(q);
	}

	@Override
	public Stream<Entity> findAll(String entityName)
	{
		return findAll(entityName, query(entityName));
	}

	@Override
	public Stream<Entity> findAll(String entityName, Query<Entity> q)
	{
		return getRepository(entityName).findAll(q);
	}

	@Override
	public Entity findOneById(String entityName, Object id)
	{
		return getRepository(entityName).findOneById(id);
	}

	@Override
	public Entity findOne(String entityName, Query<Entity> q)
	{
		return getRepository(entityName).findOne(q);
	}

	@Override
	@Transactional
	public void add(String entityName, Entity entity)
	{
		getRepository(entityName).add(entity);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Entity> void add(String entityName, Stream<E> entities)
	{
		getRepository(entityName).add((Stream<Entity>) entities);
	}

	@Override
	@Transactional
	public void update(String entityName, Entity entity)
	{
		getRepository(entityName).update(entity);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Entity> void update(String entityName, Stream<E> entities)
	{
		getRepository(entityName).update((Stream<Entity>) entities);
	}

	@Override
	@Transactional
	public void delete(String entityName, Entity entity)
	{
		getRepository(entityName).delete(entity);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Entity> void delete(String entityName, Stream<E> entities)
	{
		getRepository(entityName).delete((Stream<Entity>) entities);
	}

	@Override
	@Transactional
	public void deleteById(String entityName, Object id)
	{
		getRepository(entityName).deleteById(id);
	}

	@Override
	@Transactional
	public void deleteAll(String entityName, Stream<Object> ids)
	{
		getRepository(entityName).deleteAll(ids);
	}

	@Override
	@Transactional
	public void deleteAll(String entityName)
	{
		getRepository(entityName).deleteAll();
		LOG.info("All entities of repository [{}] deleted by user [{}]", entityName, getCurrentUsername());
	}

	@Override
	public Repository<Entity> getRepository(String entityName)
	{
		return metaDataService.getRepository(entityName);
	}

	@SuppressWarnings("unchecked")
	public <E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass)
	{
		return (Repository<E>) getRepository(entityName);
	}

	@Override
	public Query<Entity> query(String entityName)
	{
		return new QueryImpl<>(getRepository(entityName));
	}

	@Override
	public <E extends Entity> Query<E> query(String entityName, Class<E> entityClass)
	{
		return new QueryImpl<>(getRepository(entityName, entityClass));
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Query<E> q, Class<E> clazz)
	{
		return getRepository(entityName, clazz).findAll(q);
	}

	@Override
	public <E extends Entity> E findOneById(String entityName, Object id, Class<E> clazz)
	{
		return getRepository(entityName, clazz).findOneById(id);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Query<E> q, Class<E> clazz)
	{
		return getRepository(entityName, clazz).findOne(q);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Class<E> clazz)
	{
		return findAll(entityName, query(entityName, clazz), clazz);
	}

	@Override
	public AggregateResult aggregate(String entityName, AggregateQuery aggregateQuery)
	{
		return getRepository(entityName).aggregate(aggregateQuery);
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
	public Entity findOneById(String entityName, Object id, Fetch fetch)
	{
		return getRepository(entityName).findOneById(id, fetch);
	}

	@Override
	public <E extends Entity> E findOneById(String entityName, Object id, Fetch fetch, Class<E> clazz)
	{
		return getRepository(entityName, clazz).findOneById(id, fetch);
	}

	@Override
	public Stream<Entity> findAll(String entityName, Stream<Object> ids)
	{
		return getRepository(entityName).findAll(ids);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Class<E> clazz)
	{
		return getRepository(entityName, clazz).findAll(ids);
	}

	@Override
	public Stream<Entity> findAll(String entityName, Stream<Object> ids, Fetch fetch)
	{
		return getRepository(entityName).findAll(ids, fetch);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Fetch fetch, Class<E> clazz)
	{
		return getRepository(entityName, clazz).findAll(ids, fetch);
	}
}
