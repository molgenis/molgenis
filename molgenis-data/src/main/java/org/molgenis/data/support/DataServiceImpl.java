package org.molgenis.data.support;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Manageable;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.MetaDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Implementation of the DataService interface
 */

public class DataServiceImpl implements DataService
{
	private static final Logger LOG = LoggerFactory.getLogger(DataServiceImpl.class);

	private final Map<String, CrudRepository> repositories;
	private final Set<String> repositoryNames;
	private MetaDataService metaDataService;

	public DataServiceImpl()
	{
		this.repositories = new LinkedHashMap<String, CrudRepository>();
		this.repositoryNames = new TreeSet<String>();
	}

	/**
	 * For testing purposes
	 */
	public void resetRepositories()
	{
		repositories.clear();
		repositoryNames.clear();
	}

	public void setMetaDataService(MetaDataService metaDataService)
	{
		this.metaDataService = metaDataService;
	}

	@Override
	public void addRepository(CrudRepository newRepository)
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
	public Iterable<Entity> findAll(String entityName)
	{
		return findAll(entityName, new QueryImpl());
	}

	@Override
	public Iterable<Entity> findAll(String entityName, Query q)
	{
		return getRepository(entityName).findAll(q);
	}

	@Override
	public Iterable<Entity> findAll(String entityName, Iterable<Object> ids)
	{
		return getRepository(entityName).findAll(ids);
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
		return getRepository(entityName).findOne(id);
	}

	@Override
	public Entity findOne(String entityName, Query q)
	{
		return getRepository(entityName).findOne(q);
	}

	@Override
	public void add(String entityName, Entity entity)
	{
		getRepository(entityName).add(entity);
	}

	@Override
	public void add(String entityName, Iterable<? extends Entity> entities)
	{
		getRepository(entityName).add(entities);
	}

	@Override
	public void update(String entityName, Entity entity)
	{
		getRepository(entityName).update(entity);
	}

	@Override
	public void update(String entityName, Iterable<? extends Entity> entities)
	{
		getRepository(entityName).update(entities);
	}

	@Override
	public void delete(String entityName, Entity entity)
	{
		getRepository(entityName).delete(entity);
	}

	@Override
	public void delete(String entityName, Iterable<? extends Entity> entities)
	{
		getRepository(entityName).delete(entities);
	}

	@Override
	public void delete(String entityName, Object id)
	{
		getRepository(entityName).deleteById(id);
	}

	@Override
	public void deleteAll(String entityName)
	{
		getRepository(entityName).deleteAll();
	}

	@Override
	public CrudRepository getRepository(String entityName)
	{
		CrudRepository repository = repositories.get(entityName.toLowerCase());
		if (repository == null) throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		else return repository;
	}

	@Override
	public Manageable getManageableRepository(String entityName)
	{
		Repository repository = getRepository(entityName);
		if (repository instanceof Manageable)
		{
			return (Manageable) repository;
		}
		throw new MolgenisDataException("Repository [" + repository.getName() + "] is not Manageable");
	}

	@Override
	public Query query(String entityName)
	{
		return new QueryImpl(getRepository(entityName));
	}

	@Override
	public Iterable<Class<? extends Entity>> getEntityClasses()
	{
		List<Class<? extends Entity>> entityClasses = new ArrayList<Class<? extends Entity>>();
		for (String entityName : getEntityNames())
		{
			Repository repo = getRepository(entityName);
			entityClasses.add(repo.getEntityMetaData().getEntityClass());
		}

		return entityClasses;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Query q, Class<E> clazz)
	{
		return getRepository(entityName).findAll(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Iterable<Object> ids, Class<E> clazz)
	{
		return getRepository(entityName).findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Object id, Class<E> clazz)
	{
		return getRepository(entityName).findOne(id, clazz);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Query q, Class<E> clazz)
	{
		return getRepository(entityName).findOne(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Class<E> clazz)
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

}
