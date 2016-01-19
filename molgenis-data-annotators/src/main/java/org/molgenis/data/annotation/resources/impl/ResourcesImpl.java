package org.molgenis.data.annotation.resources.impl;

import static java.util.stream.Collectors.toMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResourcesImpl implements Resources
{
	private static final Logger LOG = LoggerFactory.getLogger(ResourcesImpl.class);

	@Autowired
	private DataService dataService;

	private Map<String, Resource> resources = null;

	@Autowired
	private void setResources(List<Resource> resources)
	{
		this.resources = resources.stream().collect(toMap(r -> r.getName(), r -> r));
	}

	@Override
	public boolean hasRepository(String entityName)
	{
		return dataService.hasRepository(entityName)
				|| (resources.containsKey(entityName) && resources.get(entityName).isAvailable());
	}

	@Override
	public Iterable<Entity> findAll(String name, Query q)
	{
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
		return new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return dataService.findAll(name, q).iterator();
			}
		};
	}

	@Override
	public Set<String> getResourcesNames()
	{
		return resources.keySet();
	}

}
