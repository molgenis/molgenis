package org.molgenis.data.support;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import org.springframework.util.StringUtils;

/**
 * Implementation of the DataService interface
 */
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
		int index = url.indexOf("://");
		if (index == -1)
		{
			throw new MolgenisDataException("Incorrect url format should be of format prefix://");
		}

		String prefix = url.substring(0, index);
		EntitySourceFactory entitySourceFactory = entitySourceFactoryByUrlPrefix.get(prefix);
		if (entitySourceFactory == null)
		{
			throw new MolgenisDataException("Unknown EntitySource url prefix [" + prefix + "]");
		}

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
		return getQueryableRepository(entityName).count(q);
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(String entityName, Query q)
	{
		return this.<E> getQueryableRepository(entityName).findAll(q);
	}

	@Override
	public <E extends Entity> E findOne(String entityName, Integer id)
	{
		return this.<E> getQueryableRepository(entityName).findOne(id);
	}

	@SuppressWarnings("unchecked")
	private <E extends Entity> Queryable<E> getQueryableRepository(String entityName)
	{
		Repository<? extends Entity> repo = getRepositoryByEntityName(entityName);
		if (!(repo instanceof Queryable))
		{
			throw new MolgenisDataException("Repository of [" + entityName + "] isn't queryable");
		}

		return (Queryable<E>) repo;
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
}
