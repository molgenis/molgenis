package org.molgenis.data.elasticsearch;

import java.util.Iterator;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("ElasticsearchRepositoryCollection")
public class ElasticsearchRepositoryCollection implements RepositoryCollection
{
	public static final String INDEX_NAME = "molgenis";

	private final Client client;

	@Autowired
	public ElasticsearchRepositoryCollection(Client client)
	{
		if (client == null) throw new IllegalArgumentException("client is null");
		this.client = client;
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		GetMappingsResponse getMappingsResponse = client.admin().indices().prepareGetMappings(INDEX_NAME).execute()
				.actionGet();
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
		return new ElasticsearchRepository(client, INDEX_NAME, name);
	}
}
