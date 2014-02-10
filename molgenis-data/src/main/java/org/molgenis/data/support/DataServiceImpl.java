package org.molgenis.data.support;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositorySource;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Implementation of the DataService interface
 */
@Component
public class DataServiceImpl implements DataService
{
	private final List<Repository> repositories = Lists.newArrayList();
	private final Map<String, Class<? extends FileRepositorySource>> fileRepositorySources = Maps.newHashMap();

	@Override
	public void addRepository(Repository newRepository)
	{
		for (Repository repository : repositories)
		{
			if (repository.getName().equalsIgnoreCase(newRepository.getName()))
			{
				throw new MolgenisDataException("Entity [" + repository.getName() + "] already registered.");
			}
		}

		repositories.add(newRepository);
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return Lists.transform(repositories, new Function<Repository, String>()
		{
			@Override
			public String apply(Repository repository)
			{
				return repository.getName();
			}
		});
	}

	@Override
	public Repository getRepositoryByEntityName(String entityName)
	{
		for (Repository repository : repositories)
		{
			if (repository.getName().equalsIgnoreCase(entityName))
			{
				return repository;
			}
		}

		throw new UnknownEntityException("Unknown entity [" + entityName + "]");
	}

	@Override
	public Repository getRepositoryByUrl(String url)
	{
		for (Repository repository : repositories)
		{
			if (repository.getUrl().equalsIgnoreCase(url))
			{
				return repository;
			}
		}

		return null;
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
	public Iterable<Entity> findAll(String entityName, Iterable<Integer> ids)
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
	public Entity findOne(String entityName, Integer id)
	{
		return getQueryable(entityName).findOne(id);
	}

	@Override
	public Entity findOne(String entityName, Query q)
	{
		return getQueryable(entityName).findOne(q);
	}

	@Override
	public Integer add(String entityName, Entity entity)
	{
		return getWritable(entityName).add(entity);
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
		Updateable updateable = getUpdateable(entityName);
		updateable.delete(entity);
	}

	@Override
	public void delete(String entityName, Iterable<? extends Entity> entities)
	{
		getUpdateable(entityName).delete(entities);
	}

	@Override
	public void delete(String entityName, int id)
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
	public Iterable<Class<? extends Entity>> getEntityClasses()
	{
		List<Class<? extends Entity>> entityClasses = new ArrayList<Class<? extends Entity>>();
		for (String entityName : getEntityNames())
		{
			Repository repo = getRepositoryByEntityName(entityName);
			entityClasses.add(repo.getEntityClass());
		}

		return entityClasses;
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Query q, Class<E> clazz)
	{
		return getQueryable(entityName).findAll(q, clazz);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Iterable<Integer> ids, Class<E> clazz)
	{
		return getQueryable(entityName).findAll(ids, clazz);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Integer id, Class<E> clazz)
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
	public void addFileRepositorySourceClass(Class<? extends FileRepositorySource> clazz, Set<String> fileExtensions)
	{
		for (String extension : fileExtensions)
		{
			fileRepositorySources.put(extension.toLowerCase(), clazz);
		}
	}

	@Override
	public FileRepositorySource createFileRepositorySource(File file)
	{
		String extension = StringUtils.getFilenameExtension(file.getName());
		Class<? extends FileRepositorySource> clazz = fileRepositorySources.get(extension.toLowerCase());
		if (clazz == null)
		{
			throw new MolgenisDataException("Unknown extension '" + extension + "'");
		}

		Constructor<? extends FileRepositorySource> ctor;
		try
		{
			ctor = clazz.getConstructor(File.class);
		}
		catch (Exception e)
		{
			throw new MolgenisDataException("Exception creating [" + clazz
					+ "]  missing constructor FileRepositorySource(File file)");
		}

		return BeanUtils.instantiateClass(ctor, file);
	}

	@Override
	public void addRepositories(RepositorySource repositorySource)
	{
		for (Repository repository : repositorySource.getRepositories())
		{
			addRepository(repository);
		}
	}

}
