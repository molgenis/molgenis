package org.molgenis.data.elasticsearch;

import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownAttributeException;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataImpl;
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
		return Iterators.transform(repositories.values().iterator(),
				new Function<AbstractElasticsearchRepository, Repository<Entity>>()
				{
					@Override
					public Repository<Entity> apply(AbstractElasticsearchRepository input)
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
		EntityMetaData entityMetaData;
		try
		{
			entityMetaData = (EntityMetaData) dataService.getEntityMetaData(entityName);
		}
		catch (ClassCastException ex)
		{
			throw new RuntimeException("Cannot cast EntityMetaData to EntityMetaData " + ex);
		}
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		entityMetaData.addAttribute(attribute);
		searchService.createMappings(entityMetaData);
	}

	@Override
	public void deleteAttribute(String entityName, String attributeName)
	{
		EntityMetaData entityMetaData = dataService.getMeta().getEntityMetaData(entityName);
		if (entityMetaData == null) throw new UnknownEntityException(String.format("Unknown entity '%s'", entityName));

		EntityMetaData EntityMetaData = new EntityMetaDataImpl(dataService.getMeta().getEntityMetaData(entityName));
		AttributeMetaData attr = entityMetaData.getAttribute(attributeName);
		if (attr == null) throw new UnknownAttributeException(String.format("Unknown attribute '%s' of entity '%s'",
				attributeName, entityName));

		EntityMetaData.removeAttribute(attr);
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
		if (null == name) return false;
		Iterator<String> entityNames = getEntityNames().iterator();
		while (entityNames.hasNext())
		{
			if (entityNames.next().equals(name)) return true;
		}
		return false;
	}

}
