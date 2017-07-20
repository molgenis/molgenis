package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

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
	private ClientFactory()
	{
	}

	static Client createClient(String clusterName, List<InetSocketAddress> inetAddresses)
	{
		return createClient(clusterName, inetAddresses, null);
	}

	private static Client createClient(String clusterName, List<InetSocketAddress> inetAddresses,
			@SuppressWarnings("SameParameterValue") Map<String, String> settings)
	{
		Settings clientSettings = createSettings(clusterName, settings);
		InetSocketTransportAddress[] socketTransportAddresses = createInetTransportAddresses(inetAddresses);

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

	private static Settings createSettings(String clusterName, Map<String, String> settings)
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
