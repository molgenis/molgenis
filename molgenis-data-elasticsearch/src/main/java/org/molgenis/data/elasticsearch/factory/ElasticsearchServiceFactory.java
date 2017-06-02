package org.molgenis.data.elasticsearch.factory;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Factory for creating an Elasticsearch server service. An elastic search config file named
 * 'elasticsearch.yml' must be on the classpath
 *
 * @author erwin
 */
public class ElasticsearchServiceFactory implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchServiceFactory.class);

	private static final String CONFIG_FILE_NAME = "elasticsearch.yml";
	private final Client client;

	/**
	 * Create an Elasticsearch server service with the given index name using 'elasticsearch.yml' and provided
	 * settings. The provided settings override settings specified in 'elasticsearch.yml'
	 *
	 * @param providedSettings
	 */
	public ElasticsearchServiceFactory(Map<String, String> providedSettings)
	{

		File file = ResourceUtils.getFile(getClass(), "/" + CONFIG_FILE_NAME);
		Settings.Builder builder;
		try
		{
			builder = Settings.builder().loadFromPath(file.toPath());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error loading Elasticsearch settings from file " + CONFIG_FILE_NAME);
		}
		if (providedSettings != null) builder.put(providedSettings);

		Settings settings = builder.build();

		client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress("127.0.0.1", 9300)));

		LOG.info("Connected to Elasticsearch server, data path=[" + settings.get("path.data") + "]");
	}

	public ElasticsearchService create(DataService dataService, ElasticsearchEntityFactory elasticsearchEntityFactory,
			DocumentIdGenerator documentIdGenerator)
	{
		return new ElasticsearchService(client, dataService, elasticsearchEntityFactory, documentIdGenerator);
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
	}

}
