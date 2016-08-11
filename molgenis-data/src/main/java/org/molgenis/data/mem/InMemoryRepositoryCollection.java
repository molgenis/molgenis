package org.molgenis.data.mem;

import static com.google.common.collect.Sets.immutableEnumSet;
import static org.molgenis.data.RepositoryCollectionCapability.WRITABLE;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollectionCapability;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.AbstractRepositoryCollection;

/**
 * For testing purposis
 */
public class InMemoryRepositoryCollection extends AbstractRepositoryCollection
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
	public void deleteRepository(EntityMetaData entityMeta)
	{
		repos.remove(entityMeta.getName());
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Set<RepositoryCollectionCapability> getCapabilities()
	{
		return immutableEnumSet(EnumSet.of(WRITABLE));
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
