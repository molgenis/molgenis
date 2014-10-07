package org.molgenis.data.support;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Aggregateable;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Implementation of the DataService interface
 */
@Component
public class DataServiceImpl implements DataService
{
	private static final Logger LOG = Logger.getLogger(DataServiceImpl.class);

	private final Map<String, Repository> repositories;
	private final Set<String> repositoryNames;

	public DataServiceImpl()
	{
		this.repositories = new LinkedHashMap<String, Repository>();
		this.repositoryNames = new TreeSet<String>();
	}

	@Override
	public void addRepository(Repository newRepository)
	{
		String repositoryName = newRepository.getName();
		if (repositories.containsKey(repositoryName.toLowerCase()))
		{
			throw new MolgenisDataException("Entity [" + repositoryName + "] already registered.");
		}
		if (LOG.isDebugEnabled()) LOG.debug("Adding repository [" + repositoryName + "]");
		repositoryNames.add(repositoryName);
		repositories.put(repositoryName.toLowerCase(), newRepository);
	}

	@Override
	public void removeRepository(String repositoryName)
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
		Repository repository = repositories.get(entityName.toLowerCase());
		if (repository == null) throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		return repository.getEntityMetaData();
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return Iterables.filter(repositoryNames, new Predicate<String>()
		{
			@Override
			public boolean apply(String entityName)
			{
				return currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", "ROLE_ENTITY_COUNT_" + entityName.toUpperCase());
			}
		});
	}

	@Override
	public Repository getRepositoryByEntityName(String entityName)
	{
		Repository repository = repositories.get(entityName.toLowerCase());
		if (repository == null) throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		else return repository;
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		return repositories.containsKey(entityName.toLowerCase());
	}

	@Override
	public long count(String entityName, Query q)
	{
		return getQueryable(entityName).count(q);
	}

	@Override
	public Iterable<Entity> findAll(String entityName)
	{
		return findAll(entityName, new QueryImpl());
	}

	@Override
	public Iterable<Entity> findAll(String entityName, Query q)
	{
		return getQueryable(entityName).findAll(q);
	}

	@Override
	public Iterable<Entity> findAll(String entityName, Iterable<Object> ids)
	{
		return getQueryable(entityName).findAll(ids);
	}

	@Override
	public List<Entity> findAllAsList(String entityName, Query q)
	{
		Iterable<Entity> iterable = findAll(entityName, q);
		return Lists.newArrayList(iterable);
	}

	@Override
	public Entity findOne(String entityName, Object id)
	{
		return getQueryable(entityName).findOne(id);
	}

	@Override
	public Entity findOne(String entityName, Query q)
	{
		return getQueryable(entityName).findOne(q);
	}

	@Override
	public void add(String entityName, Entity entity)
	{
		getWritable(entityName).add(entity);
	}

	@Override
	public void add(String entityName, Iterable<? extends Entity> entities)
	{
		getWritable(entityName).add(entities);
	}

	@Override
	public void update(String entityName, Entity entity)
	{
		getUpdateable(entityName).update(entity);
	}

	@Override
	public void update(String entityName, Iterable<? extends Entity> entities)
	{
		Updateable updateable = getUpdateable(entityName);
		updateable.update(entities);
	}

	@Override
	public void delete(String entityName, Entity entity)
	{
		getUpdateable(entityName).delete(entity);
	}

	@Override
	public void delete(String entityName, Iterable<? extends Entity> entities)
	{
		getUpdateable(entityName).delete(entities);
	}

	@Override
	public void delete(String entityName, Object id)
	{
		getUpdateable(entityName).deleteById(id);
	}

	private <E extends Entity> Queryable getQueryable(String entityName)
	{
		Repository repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Queryable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't queryable");
		}

		return (Queryable) repo;
	}

	private Writable getWritable(String entityName)
	{
		Repository repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Writable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't writable");
		}

		return (Writable) repo;
	}

	private Updateable getUpdateable(String entityName)
	{
		Repository repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Updateable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't updateable");
		}

		return (Updateable) repo;
	}

	@Override
	public CrudRepository getCrudRepository(String entityName)
	{
		Repository repository = getRepositoryByEntityName(entityName);
		if (repository instanceof CrudRepository)
		{
			return (CrudRepository) repository;
		}

		throw new MolgenisDataException("Repository [" + repository.getName() + "] isn't a CrudRepository");
	}

	@Override
	public Writable getWritableRepository(String entityName)
	{
		Repository repository = getRepositoryByEntityName(entityName);
		if (repository instanceof Writable)
		{
			return (Writable) repository;
		}
		throw new MolgenisDataException("Repository [" + repository.getName() + "] is not Writable");
	}

	@Override
	public Queryable getQueryableRepository(String entityName)
	{
		Repository repository = getRepositoryByEntityName(entityName);
		if (repository instanceof Queryable)
		{
			return (Queryable) repository;
		}
		throw new MolgenisDataException("Repository [" + repository.getName() + "] is not Queryable");

	}

	@Override
	public Query query(String entityName)
	{
		return new QueryImpl(getQueryableRepository(entityName));
	}

	@Override
	public Iterable<Class<? extends Entity>> getEntityClasses()
	{
		List<Class<? extends Entity>> entityClasses = new ArrayList<Class<? extends Entity>>();
		for (String entityName : getEntityNames())
		{
			Repository repo = getRepositoryByEntityName(entityName);
			entityClasses.add(repo.getEntityMetaData().getEntityClass());
		}

		return entityClasses;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Query q, Class<E> clazz)
	{
		return getQueryable(entityName).findAll(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Iterable<Object> ids, Class<E> clazz)
	{
		return getQueryable(entityName).findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Object id, Class<E> clazz)
	{
		return getQueryable(entityName).findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Query q, Class<E> clazz)
	{
		return getQueryable(entityName).findOne(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Class<E> clazz)
	{
		return findAll(entityName, new QueryImpl(), clazz);
	}

	@Override
	public AggregateResult aggregate(String entityName, AggregateQuery aggregateQuery)
	{
		Repository repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Aggregateable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't aggregateable");
		}

		return ((Aggregateable) repo).aggregate(aggregateQuery);
	}
}
