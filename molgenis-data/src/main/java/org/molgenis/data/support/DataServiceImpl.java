package org.molgenis.data.support;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Implementation of the DataService interface
 */

public class DataServiceImpl implements DataService
{
	private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

	private final ConcurrentMap<String, Repository<Entity>> repositories;
	private final Set<String> repositoryNames;
	private MetaDataService metaDataService;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	public DataServiceImpl()
	{
		this(new NonDecoratingRepositoryDecoratorFactory());
	}

	public DataServiceImpl(RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		this.repositories = Maps.newConcurrentMap();
		this.repositoryNames = new TreeSet<String>();
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
	}

	/**
	 * For testing purposes
	 */
	public synchronized void resetRepositories()
	{
		repositories.clear();
		repositoryNames.clear();
	}

	@Override
	public void setMeta(MetaDataService metaDataService)
	{
		this.metaDataService = metaDataService;
	}

	public synchronized void addRepository(Repository<Entity> newRepository)
	{
		String repositoryName = newRepository.getName();
		if (repositories.containsKey(repositoryName.toLowerCase()))
		{
			throw new MolgenisDataException("Entity [" + repositoryName + "] already registered.");
		}
		if (LOG.isDebugEnabled()) LOG.debug("Adding repository [" + repositoryName + "]");
		repositoryNames.add(repositoryName);

		Repository<Entity> decoratedRepo = repositoryDecoratorFactory.createDecoratedRepository(newRepository);
		repositories.put(repositoryName.toLowerCase(), decoratedRepo);
	}

	public synchronized void removeRepository(String repositoryName)
	{
		if (null == repositoryName)
		{
			throw new MolgenisDataException("repositoryName may not be null");
		}

		if (!repositories.containsKey(repositoryName.toLowerCase()))
		{
			throw new MolgenisDataException("Repository [" + repositoryName + "] doesn't exists");
		}
		else
		{
			if (LOG.isDebugEnabled()) LOG.debug("Removing repository [" + repositoryName + "]");
			repositoryNames.remove(repositoryName);
			repositories.remove(repositoryName.toLowerCase());
		}
	}

	@Override
	public EntityMetaData getEntityMetaData(String entityName)
	{
		Repository<Entity> repository = getRepository(entityName);
		return repository.getEntityMetaData();
	}

	@Override
	public synchronized Stream<String> getEntityNames()
	{
		return Lists.newArrayList(repositoryNames).stream().filter(entityName -> currentUserHasRole("ROLE_SU",
				"ROLE_SYSTEM", "ROLE_ENTITY_COUNT_" + entityName.toUpperCase()));
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		return repositories.containsKey(entityName.toLowerCase());
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
	public void deleteAll(String entityName)
	{
		getRepository(entityName).deleteAll();
		LOG.info("All entities of repository [{}] deleted by user [{}]", entityName, getCurrentUsername());
	}

	@Override
	public Repository<Entity> getRepository(String entityName)
	{
		Repository<Entity> repository = repositories.get(entityName.toLowerCase());
		if (repository == null) throw new UnknownEntityException("Unknown entity [" + entityName + "]");

		return repository;
	}

	@SuppressWarnings("unchecked")
	public <E extends Entity> Repository<E> getRepository(String entityName, Class<E> entityClass)
	{
		Repository<Entity> untypedRepo = getRepository(entityName);
		return new TypedRepositoryDecorator<>(untypedRepo, entityClass);
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
		Entity entity = getRepository(entityName, clazz).findOne(q);
		if (entity == null) return null;
		return EntityUtils.convert(entity, clazz, this);
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
		return Lists.newArrayList(repositories.values()).iterator();
	}

	@Override
	public Stream<Entity> stream(String entityName, Fetch fetch)
	{
		return getRepository(entityName).stream(fetch);
	}

	@Override
	public <E extends Entity> Stream<E> stream(String entityName, Fetch fetch, Class<E> clazz)
	{
		Stream<Entity> entities = getRepository(entityName).stream(fetch);
		return entities.map(entity -> {
			return EntityUtils.convert(entity, clazz, this);
		});
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
		Entity entity = getRepository(entityName).findOneById(id, fetch);
		if (entity == null) return null;
		return EntityUtils.convert(entity, clazz, this);
	}

	@Override
	public void addEntityListener(String entityName, EntityListener entityListener)
	{
		getRepository(entityName).addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(String entityName, EntityListener entityListener)
	{
		getRepository(entityName).removeEntityListener(entityListener);
	}

	@Override
	public Stream<Entity> findAll(String entityName, Stream<Object> ids)
	{
		return getRepository(entityName).findAll(ids);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Class<E> clazz)
	{
		Stream<Entity> entities = getRepository(entityName).findAll(ids);
		return entities.map(entity -> {
			return EntityUtils.convert(entity, clazz, this);
		});
	}

	@Override
	public Stream<Entity> findAll(String entityName, Stream<Object> ids, Fetch fetch)
	{
		return getRepository(entityName).findAll(ids, fetch);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Stream<Object> ids, Fetch fetch, Class<E> clazz)
	{
		Stream<Entity> entities = getRepository(entityName).findAll(ids, fetch);
		return entities.map(entity -> {
			return EntityUtils.convert(entity, clazz, this);
		});
	}

	@Override
	public Repository<Entity> copyRepository(Repository<Entity> repository, String newRepositoryId, String newRepositoryLabel)
	{
		return copyRepository(repository, newRepositoryId, newRepositoryLabel, new QueryImpl<Entity>());
	}

	@Override
	public Repository<Entity> copyRepository(Repository<Entity> repository, String newRepositoryId, String newRepositoryLabel,
			Query<Entity> query)
	{
		LOG.info("Creating a copy of " + repository.getName() + " repository, with ID: " + newRepositoryId
				+ ", and label: " + newRepositoryLabel);
		EntityMetaData emd = EntityMetaData.newInstance(repository.getEntityMetaData());
		emd.setName(newRepositoryId);
		emd.setLabel(newRepositoryLabel);
		Repository<Entity> repositoryCopy = metaDataService.addEntityMeta(emd);
		try
		{

			repositoryCopy.add(repository.findAll(query));
			return repositoryCopy;
		}
		catch (RuntimeException e)
		{
			if (repositoryCopy != null)
			{
				metaDataService.deleteEntityMeta(emd.getName());
			}
			throw e;
		}
	}
}
