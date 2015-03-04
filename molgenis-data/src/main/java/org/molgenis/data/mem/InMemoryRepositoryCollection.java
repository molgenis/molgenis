package org.molgenis.data.mem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.formula.eval.NotImplementedException;
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
		Repository repo = new InMemoryRepository(entityMetaData);
		repos.put(entityMetaData.getName(), repo);

		return repo;
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		throw new NotImplementedException("Not implemented yet");
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
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		throw new NotImplementedException("Not implemented yet");
	}
}
