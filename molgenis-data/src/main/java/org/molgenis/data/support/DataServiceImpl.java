package org.molgenis.data.support;

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
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;

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

}
