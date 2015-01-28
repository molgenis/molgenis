package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

public abstract class AbstractSearchRepositoryCollection implements ManageableRepositoryCollection
{
	public static final String INDEX_NAME = "molgenis";

	protected final SearchService searchService;
	protected final Map<String, AbstractElasticsearchRepository> repositories = Maps.newLinkedHashMap();
	protected final DataService dataService;
	private final String name;

	public AbstractSearchRepositoryCollection(SearchService searchService, DataService dataService, String name)
	{
		if (searchService == null) throw new IllegalArgumentException("searchService is null");
		this.searchService = searchService;
		this.dataService = dataService;
		this.name = name;
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
	public Iterator<Repository> iterator()
	{
		return Iterators.transform(repositories.values().iterator(),
				new Function<AbstractElasticsearchRepository, Repository>()
				{
					@Override
					public Repository apply(AbstractElasticsearchRepository input)
					{
						return input;
					}
				});
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public abstract Repository addEntityMeta(EntityMetaData entityMeta);

	@Override
	public void deleteEntityMeta(String entityName)
	{
		// remove the repo
		AbstractElasticsearchRepository r = repositories.get(entityName);
		if (r != null)
		{
			r.drop();
			repositories.remove(entityName);
		}
	}

	@Override
	public void addAttribute(String entityName, AttributeMetaData attribute)
	{
		EntityMetaData entityMetaData = dataService.getEntityMetaData(entityName);
		try
		{
			searchService.createMappings(entityMetaData);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error creating mappings for [" + entityName + "]", e);
		}
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		addAttribute(entityName, attribute);
	}

}
