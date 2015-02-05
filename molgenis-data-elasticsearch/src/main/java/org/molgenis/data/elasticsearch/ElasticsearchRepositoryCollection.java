package org.molgenis.data.elasticsearch;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.molgenis.data.CrudRepository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection implements RepositoryCollection
{
	public static final String INDEX_NAME = "molgenis";

	private final ElasticSearchService elasticSearchService;
	private final RepositoryDecoratorFactory repositoryDecoratorFactory;

	@Autowired
	public ElasticsearchRepositoryCollection(ElasticSearchService elasticSearchService,
			RepositoryDecoratorFactory repositoryDecoratorFactory)
	{
		if (elasticSearchService == null) throw new IllegalArgumentException("elasticSearchClient is null");
		if (repositoryDecoratorFactory == null) throw new IllegalArgumentException("repositoryDecoratorFactory is null");
		this.elasticSearchService = elasticSearchService;
		this.repositoryDecoratorFactory = repositoryDecoratorFactory;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		// Return the enities with full metadata in the index
		GetMappingsResponse getMappingsResponse = elasticSearchService.getMappings();
		ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> allMappings = getMappingsResponse
				.getMappings();
		final ImmutableOpenMap<String, MappingMetaData> indexMappings = allMappings.get(INDEX_NAME);

		List<String> entityNames = Lists.newArrayList();
		Iterator<String> it = indexMappings.keysIt();
		while (it.hasNext())
		{
			String entityName = it.next();
			MappingMetaData mmd = indexMappings.get(entityName);
			try
			{
				Map<String, Object> source = mmd.getSourceAsMap();
				if (source.containsKey("_meta"))
				{
					entityNames.add(entityName);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}

		}

		return entityNames;
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

		CrudRepository repo = new ElasticsearchRepository(entityMetaData, elasticSearchService);
		return repositoryDecoratorFactory.createDecoratedRepository(repo);
	}

}
