package org.molgenis.data.examples;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;

public class MyRepositoryCollection implements RepositoryCollection
{
	private final Map<String, Repository> repositories = new LinkedHashMap<>();

	@Override
	public Iterator<Repository> iterator()
	{
		return repositories.values().iterator();
	}

	@Override
	public String getName()
	{
		return "MyRepos";
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		Repository repo = new MyRepository(entityMeta);
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepository(String name)
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
