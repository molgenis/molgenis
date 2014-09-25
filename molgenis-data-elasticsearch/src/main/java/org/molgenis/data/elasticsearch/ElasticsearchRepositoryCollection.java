package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.Iterator;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.elasticsearch.index.MappingsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection implements RepositoryCollection
{
	public static final String INDEX_NAME = "molgenis";

	private final ElasticSearchService elasticSearchService;
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

		GetMappingsResponse getMappingsResponse = elasticSearchService.getMappings();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingsResponse
				.getMappings();
		final ImmutableOpenMap<String, MappingMetaData> indexMappings = allMappings.get(INDEX_NAME);
		return new Iterable<String>()
		{
			@Override
			public Iterator<String> iterator()
			{
				return indexMappings.keysIt();
			}
		};
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		EntityMetaData entityMetaData;
		try
		{
			entityMetaData = elasticSearchService.deserializeEntityMeta(name);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		return new ElasticsearchRepository(entityMetaData, elasticSearchService);
	}
}
