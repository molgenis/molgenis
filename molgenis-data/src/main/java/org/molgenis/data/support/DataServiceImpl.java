package org.molgenis.data.support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntitySource;
import org.molgenis.data.EntitySourceFactory;
import org.molgenis.data.FileBasedEntitySourceFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.Updateable;
import org.molgenis.data.Writable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

/**
 * Implementation of the DataService interface
 */
@Component
public class DataServiceImpl implements DataService
{
	// Key: entity name, value:EntitySourceFactory
	private final Map<String, EntitySourceFactory> entitySourceFactoryByEntityName = new LinkedHashMap<String, EntitySourceFactory>();

	// Key: entity name, value:EntitySource url of the entity
	private final Map<String, String> entitySourceUrlByEntityName = new HashMap<String, String>();

	// Key: EntitySourceFactory.urlPrefix, value:EntitySource
	private final Map<String, EntitySourceFactory> entitySourceFactoryByUrlPrefix = new HashMap<String, EntitySourceFactory>();

	private final Map<String, FileBasedEntitySourceFactory> fileBasedEntitySourceFactoryByFileExtension = new HashMap<String, FileBasedEntitySourceFactory>();

	@Override
	public Iterable<String> getEntityNames()
	{
		return entitySourceFactoryByEntityName.keySet();
	}

	@Override
	public Repository<? extends Entity> getRepositoryByEntityName(String entityName)
	{
		EntitySourceFactory factory = entitySourceFactoryByEntityName.get(entityName);
		if (factory == null)
		{
			throw new UnknownEntityException("Unknown entity [" + entityName + "]");
		}

		String url = entitySourceUrlByEntityName.get(entityName);
		EntitySource entitySource = factory.create(url);

		return entitySource.getRepositoryByEntityName(entityName);
	}

	@Override
	public Iterator<EntitySource> iterator()
	{
		Set<EntitySource> entitySources = new LinkedHashSet<EntitySource>();
		for (String entityName : entitySourceUrlByEntityName.keySet())
		{
			String url = entitySourceUrlByEntityName.get(entityName);
			EntitySourceFactory factory = entitySourceFactoryByEntityName.get(entityName);
			entitySources.add(factory.create(url));
		}

		return entitySources.iterator();
	}

	/**
	 * Register a new EntitySourceFactory of an EntitySource implementation
	 */
	@Override
	public void registerFactory(EntitySourceFactory entitySourceFactory)
	{
		if (entitySourceFactoryByUrlPrefix.get(entitySourceFactory.getUrlPrefix()) != null)
		{
			throw new MolgenisDataException(entitySourceFactory.getUrlPrefix() + " already registered");
		}

		entitySourceFactoryByUrlPrefix.put(entitySourceFactory.getUrlPrefix(), entitySourceFactory);

		if (entitySourceFactory instanceof FileBasedEntitySourceFactory)
		{
			FileBasedEntitySourceFactory factory = (FileBasedEntitySourceFactory) entitySourceFactory;
			for (String fileExtension : factory.getFileExtensions())
			{
				fileBasedEntitySourceFactoryByFileExtension.put(fileExtension, factory);
			}
		}
	}

	@Override
	public void registerEntitySource(String url)
	{
		EntitySourceFactory entitySourceFactory = getEntitySourcefactory(url);
		EntitySource entitySource = entitySourceFactory.create(url);

		for (String entityName : entitySource.getEntityNames())
		{
			entitySourceFactoryByEntityName.put(entityName, entitySourceFactory);
			entitySourceUrlByEntityName.put(entityName, url);
		}
	}

	@Override
	public long count(String entityName, Query q)
	{
		return getQueryable(entityName).count(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName)
	{
		return findAll(entityName, new QueryImpl());
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Query q)
	{
		Queryable<E> queryable = getQueryable(entityName);
		return queryable.findAll(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Iterable<Integer> ids)
	{
		Queryable<E> queryable = getQueryable(entityName);
		return queryable.findAll(ids);
	}

	@Override
	public <E extends Entity> List<E> findAllAsList(String entityName, Query q)
	{
		Iterable<E> iterable = findAll(entityName, q);
		return Lists.newArrayList(iterable);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Integer id)
	{
		Queryable<E> queryable = getQueryable(entityName);
		return queryable.findOne(id);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Query q)
	{
		Queryable<E> queryable = getQueryable(entityName);
		return queryable.findOne(q);
	}

	@Override
	public <E extends Entity> void add(String entityName, E entity)
	{
		Writable<E> writable = getWritable(entityName);
		writable.add(entity);
	}

	@Override
	public <E extends Entity> void add(String entityName, Iterable<E> entities)
	{
		Writable<E> writable = getWritable(entityName);
		writable.add(entities);
	}

	@Override
	public <E extends Entity> void update(String entityName, E entity)
	{
		Updateable<E> updateable = getUpdateable(entityName);
		updateable.update(entity);
	}

	@Override
	public <E extends Entity> void update(String entityName, Iterable<E> entities)
	{
		Updateable<E> updateable = getUpdateable(entityName);
		updateable.update(entities);
	}

	@Override
	public <E extends Entity> void delete(String entityName, E entity)
	{
		Updateable<E> updateable = getUpdateable(entityName);
		updateable.delete(entity);
	}

	@Override
	public <E extends Entity> void delete(String entityName, Iterable<E> entities)
	{
		Updateable<E> updateable = getUpdateable(entityName);
		updateable.delete(entities);
	}

	@SuppressWarnings("unchecked")
	private <E extends Entity> Queryable<E> getQueryable(String entityName)
	{
		Repository<? extends Entity> repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Queryable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't queryable");
		}

		return (Queryable<E>) repo;
	}

	@SuppressWarnings("unchecked")
	private <E extends Entity> Writable<E> getWritable(String entityName)
	{
		Repository<? extends Entity> repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Writable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't writable");
		}

		return (Writable<E>) repo;
	}

	@SuppressWarnings("unchecked")
	private <E extends Entity> Updateable<E> getUpdateable(String entityName)
	{
		Repository<? extends Entity> repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Updateable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't updateable");
		}

		return (Updateable<E>) repo;
	}

	@Override
	public EntitySource createEntitySource(File file) throws IOException
	{
		if (!file.isFile())
		{
			throw new MolgenisDataException("File [" + file.getAbsolutePath() + "] is not a file");
		}

		String extension = StringUtils.getFilenameExtension(file.getName());
		FileBasedEntitySourceFactory factory = fileBasedEntitySourceFactoryByFileExtension.get(extension);
		if (factory == null)
		{
			throw new MolgenisDataException("Unknown file extension [" + extension + "]");
		}

		return factory.create(file);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends Entity> CrudRepository<E> getCrudRepository(String entityName)
	{
		Repository<? extends Entity> repository = getRepositoryByEntityName(entityName);
		if (repository instanceof CrudRepository)
		{
			return (CrudRepository<E>) repository;
		}

		throw new MolgenisDataException("Repository [" + repository.getName() + "] isn't a CrudRepository");
	}

	@Override
	public EntitySource getEntitySource(String url)
	{
		EntitySourceFactory entitySourceFactory = getEntitySourcefactory(url);
		return entitySourceFactory.create(url);
	}

	private EntitySourceFactory getEntitySourcefactory(String url)
	{
		int index = url.indexOf("://");
		if (index == -1)
		{
			throw new MolgenisDataException("Incorrect url format should be of format prefix://");
		}

		String prefix = url.substring(0, index + "://".length());
		EntitySourceFactory entitySourceFactory = entitySourceFactoryByUrlPrefix.get(prefix);
		if (entitySourceFactory == null)
		{
			throw new MolgenisDataException("Unknown EntitySource url prefix [" + prefix + "]");
		}

		return entitySourceFactory;
	}

	@Override
	public Iterable<Class<? extends Entity>> getEntityClasses()
	{
		List<Class<? extends Entity>> entityClasses = new ArrayList<Class<? extends Entity>>();
		for (String entityName : getEntityNames())
		{
			Repository<? extends Entity> repo = getRepositoryByEntityName(entityName);
			entityClasses.add(repo.getEntityClass());
		}

		return entityClasses;
	}
}
