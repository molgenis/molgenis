package org.molgenis.data.examples;

import static autovalue.shaded.com.google.common.common.collect.Sets.immutableEnumSet;
import static org.molgenis.data.RepositoryCollectionCapability.WRITABLE;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.AbstractRepositoryCollection;

public class MyRepositoryCollection extends AbstractRepositoryCollection
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
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return immutableEnumSet(EnumSet.of(WRITABLE));
	}

	@Override
	public Repository<Entity> createRepository(EntityMetaData entityMeta)
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
	public Repository<Entity> getRepository(EntityMetaData entityMetaData)
	{
		return getRepository(entityMetaData);
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

	@Override
	public void deleteRepository(EntityMetaData entityMeta)
	{
		repositories.remove(entityMeta.getName());
	}
}
