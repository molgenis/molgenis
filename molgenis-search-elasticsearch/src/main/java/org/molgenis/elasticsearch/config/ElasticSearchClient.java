package org.molgenis.elasticsearch.config;

import org.elasticsearch.client.Client;

public class ElasticSearchClient
{
	private final Client client;
	private final String indexName;

	public ElasticSearchClient(Client client, String indexName)
	{
		if (client == null) throw new IllegalArgumentException("client is null");
		if (indexName == null) throw new IllegalArgumentException("indexName is null");
		this.client = client;
		this.indexName = indexName;
	}

	public Client getClient()
	{
		return client;
	}

	public String getIndexName()
	{
		return indexName;
	}
}
