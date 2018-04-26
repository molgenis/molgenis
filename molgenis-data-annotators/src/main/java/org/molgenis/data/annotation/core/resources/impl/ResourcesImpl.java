package org.molgenis.data.annotation.core.resources.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@Component
public class ResourcesImpl implements Resources
{
	private static final Logger LOG = LoggerFactory.getLogger(ResourcesImpl.class);

	@Autowired
	private DataService dataService;

	@Autowired
	private ApplicationContext applicationContext;

	private Map<String, Resource> resources = null;

	@Override
	public boolean hasRepository(String entityTypeId)
	{
		getResources();
		return dataService.hasRepository(entityTypeId) || (resources.containsKey(entityTypeId) && resources.get(
				entityTypeId).isAvailable());
	}

	@Override
	public Iterable<Entity> findAll(String name, Query<Entity> q)
	{
		getResources();
		if (resources.containsKey(name))
		{
			// Don't check isAvailable() yet, it's too costly.
			try
			{
				return resources.get(name).findAll(q);
			}
			catch (Exception ex)
			{
				// something went wrong, find out what is the cause
				if (resources.get(name).isAvailable())
				{
					LOG.error("Error querying Resource {}.", name);
					throw ex;
				}
				LOG.warn("Resource {} is unavailable, trying dataService instead.", name);
			}
		}
		return () -> dataService.findAll(name, q).iterator();
	}

	@Override
	public Set<String> getResourcesNames()
	{
		getResources();
		return resources.keySet();
	}

	private void getResources()
	{
		if (resources == null)
		{
			this.resources = applicationContext.getBeansOfType(Resource.class)
											   .values()
											   .stream()
											   .collect(toMap(Resource::getName, r -> r));
		}
	}
}
