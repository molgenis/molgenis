package org.molgenis.data.elasticsearch.factory;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating an embedded ElasticSearch server service. An elastic search config file named
 * 'elasticsearch.yml' must be on the classpath
 * 
 * @author erwin
 * 
 */
public class EmbeddedElasticSearchServiceFactory implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(EmbeddedElasticSearchServiceFactory.class);

	private static final String CONFIG_FILE_NAME = "elasticsearch.yml";
	public static final String DEFAULT_INDEX_NAME = "molgenis";
	private final Client client;
	private final Node node;
	private final String indexName;

	public EmbeddedElasticSearchServiceFactory()
	{
		this(DEFAULT_INDEX_NAME);
	}

	/**
	 * Create an embedded ElasticSearch server service using 'elasticsearch.yml' and provided settings. The provided
	 * settings override settings specified in 'elasticsearch.yml'
	 * 
	 * @param providedSettings
	 */
	public EmbeddedElasticSearchServiceFactory(Map<String, String> providedSettings)
	{
		this(DEFAULT_INDEX_NAME, providedSettings);
	}

	public EmbeddedElasticSearchServiceFactory(String indexName)
	{
		this(indexName, null);
	}

	/**
	 * Create an embedded ElasticSearch server service with the given index name using 'elasticsearch.yml' and provided
	 * settings. The provided settings override settings specified in 'elasticsearch.yml'
	 * 
	 * @param indexName
	 * @param providedSettings
	 */
	public EmbeddedElasticSearchServiceFactory(String indexName, Map<String, String> providedSettings)
	{
		this.indexName = indexName;

		Builder builder = ImmutableSettings.settingsBuilder().loadFromClasspath(CONFIG_FILE_NAME);
		if (providedSettings != null) builder.put(providedSettings);

		Settings settings = builder.build();
		node = nodeBuilder().settings(settings).local(true).node();
		client = node.client();

		LOG.info("Embedded elasticsearch server started, data path=[" + settings.get("path.data") + "]");
	}

	public ElasticSearchService create(DataService dataService, EntityToSourceConverter entityToSourceConverter)
	{
		return new ElasticSearchService(client, indexName, dataService, entityToSourceConverter);
	}

	public Client getClient()
	{
		return client;
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
