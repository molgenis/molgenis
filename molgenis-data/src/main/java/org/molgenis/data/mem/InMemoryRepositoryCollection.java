package org.molgenis.data.mem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;

/**
 * For testing purposis
 */
public class InMemoryRepositoryCollection implements ManageableRepositoryCollection
{
	private final Map<String, Repository> repos = new HashMap<>();
	private String name = "Memory";

	public InMemoryRepositoryCollection()
	{
	}

	public InMemoryRepositoryCollection(String name)
	{
		this.name = name;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repos.keySet();
	}

	@Override
	public Repository getRepository(String name)
	{
		return repos.get(name);
	}

	@Override
	public Iterator<Repository> iterator()
	{
		return repos.values().iterator();
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMetaData)
	{
		String name = entityMetaData.getName();
		if (!repos.containsKey(name))
		{
			Repository repo = new InMemoryRepository(entityMetaData);
			repos.put(name, repo);
		}
		return repos.get(name);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		repos.remove(entityName);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		Iterator<String> entityNames = getEntityNames().iterator();
		while (entityNames.hasNext())
		{
			if (entityNames.next().equals(name)) return true;
		}
		return false;
	}
}
