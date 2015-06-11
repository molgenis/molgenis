package org.molgenis.data.annotation.resources.impl;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.springframework.beans.factory.annotation.Autowired;

public class ResourcesImpl implements Resources
{
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
	public Iterable<Entity> findAll(String entityName, Query q)
	{
		return dataService.findAll(entityName, q);
	}

}
