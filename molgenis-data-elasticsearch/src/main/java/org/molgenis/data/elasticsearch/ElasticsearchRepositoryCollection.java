package org.molgenis.data.elasticsearch;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import org.molgenis.data.*;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

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
	public Repository<Entity> addEntityMeta(EntityMetaData entityMeta)
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
	public Repository<Entity> getRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public Iterator<Repository<Entity>> iterator()
	{
		return Iterators.transform(repositories.values().iterator(), input -> input);
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

		entityMetaData.addAttributeMetaData(attribute);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityName);
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		DefaultEntityMetaData defaultEntityMetaData = new DefaultEntityMetaData(
				dataService.getMeta().getEntityMetaData(entityName));
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null) throw new UnknownAttributeException(
				String.format("Unknown attribute '%s' of entity '%s'", attributeName, entityName));

		defaultEntityMetaData.removeAttributeMetaData(attr);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public void addAttributeSync(String entityName, AttributeMetaData attribute)
	{
		addAttribute(entityName, attribute);
	}

	@Override
	public boolean hasRepository(String name)
	{
		return name != null && Iterables.contains(getEntityNames(), name);
	}

}
