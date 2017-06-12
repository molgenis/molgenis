package org.molgenis.data.elasticsearch.factory;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.ElasticsearchService;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Factory for creating an Elasticsearch server service.
 *
 * @author erwin
 */
public class ElasticsearchServiceFactory implements Closeable
{
	private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchServiceFactory.class);

	private final Client client;

	public ElasticsearchServiceFactory(String clusterName, List<InetSocketAddress> socketAddresses)
	{
		this(clusterName, socketAddresses, null);
	}

	public ElasticsearchServiceFactory(String clusterName, List<InetSocketAddress> socketAddresses,
			Map<String, String> settings)
	{
		this.client = createElasticsearchClient(clusterName, socketAddresses, settings);
		LOG.info("Connected to Elasticsearch cluster '{}' on {}", clusterName, socketAddresses.toString());
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
		catch (RuntimeException e)
		{
			LOG.error("Error closing Elasticsearch client", e);
		}
	}

	private Client createElasticsearchClient(String clusterName, List<InetSocketAddress> socketAddresses,
			Map<String, String> settings)
	{
		Settings clientSettings = createSettings(clusterName, settings);
		InetSocketTransportAddress[] socketTransportAddresses = createInetSocketTransportAddresses(socketAddresses);

		TransportClient transportClient = new PreBuiltTransportClient(clientSettings).addTransportAddresses(
				socketTransportAddresses);

		if (transportClient.connectedNodes().isEmpty())
		{
			throw new RuntimeException(
					format("Failed to connect to Elasticsearch cluster '%s' on %s. Is Elasticsearch running?",
							clusterName, Arrays.toString(socketTransportAddresses)));
		}
		return transportClient;
	}

	private Settings createSettings(String clusterName, Map<String, String> settings)
	{
		if (clusterName == null)
		{
			throw new NullPointerException("clusterName cannot be null");
		}

		Settings.Builder builder = Settings.builder();
		builder.put("cluster.name", clusterName);
		if (settings != null)
		{
			builder.put(settings);
		}
		return builder.build();
	}

	private InetSocketTransportAddress[] createInetSocketTransportAddresses(List<InetSocketAddress> socketAddresses)
	{
		if (socketAddresses == null)
		{
			throw new NullPointerException("socketAddresses cannot be null");
		}
		if (socketAddresses.isEmpty())
		{
			throw new IllegalArgumentException("socketAddresses cannot be empty");
		}
		return socketAddresses.stream().map(InetSocketTransportAddress::new).toArray(InetSocketTransportAddress[]::new);
	}
}
