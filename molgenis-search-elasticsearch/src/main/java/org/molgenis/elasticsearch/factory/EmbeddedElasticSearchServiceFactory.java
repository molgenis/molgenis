package org.molgenis.elasticsearch.factory;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.Closeable;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.molgenis.elasticsearch.ElasticSearchService;

/**
 * Factory for creating an embedded ElasticSearch server service. An elastic
 * search config file named 'elasticsearch.yml' must be on the classpath
 * 
 * @author erwin
 * 
 */
public class EmbeddedElasticSearchServiceFactory implements Closeable
{
	private static final Logger LOG = Logger.getLogger(EmbeddedElasticSearchServiceFactory.class);
	private static final String CONFIG_FILE_NAME = "elasticsearch.yml";
	private static final String DEFAULT_INDEX_NAME = "molgenis";
	private final Client client;
	private final Node node;
	private final String indexName;

	public EmbeddedElasticSearchServiceFactory()
	{
		this(DEFAULT_INDEX_NAME);
	}

	public EmbeddedElasticSearchServiceFactory(String indexName)
	{
		this.indexName = indexName;

		Settings settings = ImmutableSettings.settingsBuilder().loadFromClasspath(CONFIG_FILE_NAME).build();
		node = nodeBuilder().settings(settings).local(true).node();
		client = node.client();

		LOG.info("Embedded elasticsearch server started, data path=[" + settings.get("path.data") + "]");
	}

	public ElasticSearchService create()
	{
		return new ElasticSearchService(client, indexName);
	}

	@Override
	public void close() throws IOException
	{

		try
		{
			client.close();
		}
		catch (Exception e)
		{
			LOG.error("Error closing client", e);
		}

		try
		{
			node.close();
		}
		catch (Exception e)
		{
			LOG.error("Error closing node", e);
		}

		LOG.info("Elastic search server stopped");
	}

}
