package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection implements ManageableRepositoryCollection
{
	public static final String NAME = "ElasticSearch";
	private final SearchService searchService;
	private final DataService dataService;
	private final Map<String, AbstractElasticsearchRepository> repositories = Maps.newLinkedHashMap();

	@Autowired
	public ElasticsearchRepositoryCollection(SearchService searchService, DataService dataService)
	{
		this.searchService = searchService;
		this.dataService = dataService;
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		ElasticsearchRepository repo = new ElasticsearchRepository(entityMeta, searchService);
		if (!searchService.hasMapping(entityMeta)) repo.create();
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

	@Override
	public String getName()
	{
		return NAME;
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
		DefaultEntityMetaData entityMetaData;
		try
		{
			entityMetaData = (DefaultEntityMetaData) dataService.getEntityMetaData(entityName);
		}
		catch (ClassCastException ex)
		{
			throw new RuntimeException("Cannot cast EntityMetaData to DefaultEntityMetadata " + ex);
		}
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		try
		{
			entityMetaData.addAttributeMetaData(attribute);
			searchService.createMappings(entityMetaData);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Error creating mappings for [" + entityName + "]", e);
		}
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityName);
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(dataService.getMeta()
				.getEntityMetaData(entityName));
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null) throw new UnknownAttributeException(String.format("Unknown attribute '%s' of entity '%s'",
				attributeName, entityName));

		defaultEntityMetaData.removeAttributeMetaData(attr);

		try
		{
			searchService.createMappings(entityMetaData);
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Error creating mappings for [" + entityName + "]", e);
		}
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		addAttribute(entityName, attribute);
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
