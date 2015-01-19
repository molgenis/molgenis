package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.ManageableCrudRepositoryCollection;
import org.molgenis.data.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection implements ManageableCrudRepositoryCollection
{
	public static final String INDEX_NAME = "molgenis";
	public static final String NAME = "ElasticSearch";
	private final ElasticSearchService elasticSearchService;
	private final Map<String, ElasticsearchRepository> repositories = Maps.newLinkedHashMap();
	private final DataService dataService;

	@Autowired
	public ElasticsearchRepositoryCollection(ElasticSearchService elasticSearchService, DataService dataService)
	{
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchClient is null");
		this.elasticSearchService = elasticSearchService;
		this.dataService = dataService;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public CrudRepository getCrudRepository(String name)
	{
		return repositories.get(name);
	}

	@Override
	public Repository getRepository(String name)
	{
		return getCrudRepository(name);
	}

	@Override
	public Iterator<CrudRepository> iterator()
	{
		return Iterators.transform(repositories.values().iterator(),
				new Function<ElasticsearchRepository, CrudRepository>()
				{
					@Override
					public CrudRepository apply(ElasticsearchRepository input)
					{
						return input;
					}
				});
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public CrudRepository addEntityMeta(EntityMetaData entityMeta)
	{
		ElasticsearchRepository repo = new ElasticsearchRepository(entityMeta, elasticSearchService);
		if (!elasticSearchService.hasMapping(entityMeta)) repo.create();
		repositories.put(entityMeta.getName(), repo);

		return repo;
	}

	@Override
	public void deleteEntityMeta(String entityName)
	{
		// remove the repo
		ElasticsearchRepository r = repositories.get(entityName);
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
			elasticSearchService.createMappings(entityMetaData);
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
