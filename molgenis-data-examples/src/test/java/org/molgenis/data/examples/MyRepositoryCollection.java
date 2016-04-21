package org.molgenis.data.examples;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;

public class MyRepositoryCollection implements RepositoryCollection
{
	private final Map<String, Repository<Entity>> repositories = new LinkedHashMap<>();

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return repositories.values().iterator();
	}

	@Override
	public String getName()
	{
		return "MyRepos";
	}

	@Override
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
	{
		Repository<Entity> repo = new MyRepository(entityMeta);
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository<Entity> getRepository(String name)
	{
		return repositories.get(name);
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
