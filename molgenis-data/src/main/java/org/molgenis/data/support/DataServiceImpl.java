package org.molgenis.data.support;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

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
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Fetch;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.UnknownEntityException;
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

	private final ConcurrentMap<String, Repository> repositories;
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

	public synchronized void addRepository(Repository newRepository)
	{
		String repositoryName = newRepository.getName();
		if (repositories.containsKey(repositoryName.toLowerCase()))
		{
			throw new MolgenisDataException("Entity [" + repositoryName + "] already registered.");
		}
		if (LOG.isDebugEnabled()) LOG.debug("Adding repository [" + repositoryName + "]");
		repositoryNames.add(repositoryName);

		Repository decoratedRepo = repositoryDecoratorFactory.createDecoratedRepository(newRepository);
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
		Repository repository = getRepository(entityName);
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
	public long count(String entityName, Query q)
	{
		return getRepository(entityName).count(q);
	}

	@Override
	public Stream<Entity> findAll(String entityName)
	{
		return findAll(entityName, new QueryImpl());
	}

	@Override
	public Stream<Entity> findAll(String entityName, Query q)
	{
		return getRepository(entityName).findAll(q);
	}

	@Override
	public Entity findOne(String entityName, Object id)
	{
		return getRepository(entityName).findOne(id);
	}

	@Override
	public Entity findOne(String entityName, Query q)
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
	public void add(String entityName, Stream<? extends Entity> entities)
	{
		getRepository(entityName).add(entities);
	}

	@Override
	@Transactional
	public void update(String entityName, Entity entity)
	{
		getRepository(entityName).update(entity);
	}

	@Override
	@Transactional
	public void update(String entityName, Stream<? extends Entity> entities)
	{
		getRepository(entityName).update(entities);
	}

	@Override
	@Transactional
	public void delete(String entityName, Entity entity)
	{
		getRepository(entityName).delete(entity);
	}

	@Override
	@Transactional
	public void delete(String entityName, Stream<? extends Entity> entities)
	{
		getRepository(entityName).delete(entities);
	}

	@Override
	@Transactional
	public void delete(String entityName, Object id)
	{
		getRepository(entityName).deleteById(id);
	}

	@Override
	@Transactional
	public void deleteAll(String entityName)
	{
		getRepository(entityName).deleteAll();
	}

	@Override
	public Repository getRepository(String entityName)
	{
		Repository repository = repositories.get(entityName.toLowerCase());
		if (repository == null) throw new UnknownEntityException("Unknown entity [" + entityName + "]");

		return repository;
	}

	@Override
	public Query query(String entityName)
	{
		return new QueryImpl(getRepository(entityName));
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Query q, Class<E> clazz)
	{
		Stream<Entity> entities = getRepository(entityName).findAll(q);
		return entities.map(entity -> {
			return EntityUtils.convert(entity, clazz, this);
		});
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Object id, Class<E> clazz)
	{
		Entity entity = getRepository(entityName).findOne(id);
		if (entity == null) return null;
		return EntityUtils.convert(entity, clazz, this);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Query q, Class<E> clazz)
	{
		Entity entity = getRepository(entityName).findOne(q);
		if (entity == null) return null;
		return EntityUtils.convert(entity, clazz, this);
	}

	@Override
	public <E extends Entity> Stream<E> findAll(String entityName, Class<E> clazz)
	{
		return findAll(entityName, new QueryImpl(), clazz);
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
	public synchronized Iterator<Repository> iterator()
	{
		return Lists.newArrayList(repositories.values()).iterator();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities(String repositoryName)
	{
		return getRepository(repositoryName).getCapabilities();
	}

	@Override
	public Entity findOne(String entityName, Object id, Fetch fetch)
	{
		return getRepository(entityName).findOne(id, fetch);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Object id, Fetch fetch, Class<E> clazz)
	{
		Entity entity = getRepository(entityName).findOne(id, fetch);
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
}
