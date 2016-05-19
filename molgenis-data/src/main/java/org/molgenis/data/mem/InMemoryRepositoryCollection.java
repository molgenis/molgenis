package org.molgenis.data.mem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;

/**
 * For testing purposis
 */
public class InMemoryRepositoryCollection implements RepositoryCollection
{
	private final Map<String, Repository<Entity>> repos = new HashMap<>();
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
	public Repository<Entity> getRepository(String name)
	{
		return repos.get(name);
	}

	@Override
	public Repository<Entity> getRepository(EntityMetaData entityMetaData)
	{
		return getRepository(entityMetaData.getName());
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return repos.values().iterator();
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMetaData)
	{
		String name = entityMetaData.getName();
		if (!repos.containsKey(name))
		{
			Repository<Entity> repo = new InMemoryRepository(entityMetaData);
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
	public void deleteRepository(String entityName)
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

	@Override
	public boolean hasRepository(EntityMetaData entityMeta)
	{
		return hasRepository(entityMeta.getName());
	}
}
