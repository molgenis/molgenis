package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Creates an Elasticsearch transport client based on given configuration settings.
 */
class ClientFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(ClientFactory.class);
	private final static int MAX_CONNECTION_TRIES = 480; // Almost 24 hours when MAX_INTERVAL_MS is set to 300000
	private final static long INITIAL_CONNECTION_INTERVAL_MS = 1000;
	private final static long MAX_INTERVAL_MS = 300000; // 5 minutes

	private ClientFactory()
	{
	}

	/**
	 * Try's to create by connecting to cluster with given settings.
	 * In case the connection fail the connection is re-tried {@value #MAX_CONNECTION_TRIES} times.
	 * <p>
	 * Delay time = MIN({@value #MAX_INTERVAL_MS} times (n-th retry squared), {@value #MAX_INTERVAL_MS})
	 */
	static Client createClient(String clusterName, List<InetSocketAddress> inetAddresses,
			PreBuiltTransportClientFactory preBuiltTransportClientFactory)
	{
		return createClient(clusterName, inetAddresses, null, preBuiltTransportClientFactory);
	}

	private static Client createClient(String clusterName, List<InetSocketAddress> inetAddresses,
			@SuppressWarnings("SameParameterValue") Map<String, String> settings,
			PreBuiltTransportClientFactory preBuiltTransportClientFactory)
	{
		InetSocketTransportAddress[] socketTransportAddresses = createInetTransportAddresses(inetAddresses);

		TransportClient transportClient = preBuiltTransportClientFactory.build(clusterName, settings)
				.addTransportAddresses(socketTransportAddresses);

		int connectionTryCount = 0;
		while (transportClient.connectedNodes().isEmpty() && connectionTryCount < MAX_CONNECTION_TRIES)
		{
			connectionTryCount++;
			final long sleepTime = (long) Math
					.min(INITIAL_CONNECTION_INTERVAL_MS * Math.pow(connectionTryCount, 2), MAX_INTERVAL_MS);
			LOG.info(format("Failed to connect to Elasticsearch cluster '%s' on %s. Is Elasticsearch running?",
					clusterName, Arrays.toString(socketTransportAddresses)));
			LOG.info(format("Retry %s of %s. Waiting %s ms before next try.", String.valueOf(connectionTryCount),
					String.valueOf(MAX_CONNECTION_TRIES), String.valueOf(sleepTime)));
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e)
			{
				LOG.error(
						format("Failed to wait for connection while creating Elasticsearch connection, cluster '%s' on %s.",
								Arrays.toString(socketTransportAddresses), clusterName));
				Thread.currentThread().interrupt();
			}

			transportClient = preBuiltTransportClientFactory.build(clusterName, settings)
					.addTransportAddresses(socketTransportAddresses).addTransportAddresses(socketTransportAddresses);
		}

		if (transportClient.connectedNodes().isEmpty())
		{
			throw new RuntimeException(
					format("Failed to connect to Elasticsearch cluster '%s' on %s. Is Elasticsearch running?",
							clusterName, Arrays.toString(socketTransportAddresses)));
		}
		return transportClient;
	}

	private static InetSocketTransportAddress[] createInetTransportAddresses(List<InetSocketAddress> inetAddresses)
	{
		if (inetAddresses == null)
		{
			throw new NullPointerException("inetAddresses cannot be null");
		}
		if (inetAddresses.isEmpty())
		{
			throw new IllegalArgumentException("inetAddresses cannot be empty");
		}
		return inetAddresses.stream().map(InetSocketTransportAddress::new).toArray(InetSocketTransportAddress[]::new);
	}
}
