package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * Creates an Elasticsearch transport client based on given configuration settings.
 */
class ClientFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(ClientFactory.class);

	private final RetryTemplate retryTemplate;
	private final String clusterName;
	private final List<InetSocketAddress> inetAddresses;
	private final PreBuiltTransportClientFactory preBuiltTransportClientFactory;

	/**
	 * * @param clusterName name of the cluster
	 *
	 * @param inetAddresses                  addresses to connect to
	 * @param preBuiltTransportClientFactory {@link PreBuiltTransportClientFactory} used to create the client
	 * @param retryTemplate                  {@link RetryTemplate} to keep trying to connect
	 * @param clusterName                    name of the cluster
	 */
	public ClientFactory(RetryTemplate retryTemplate, String clusterName, List<InetSocketAddress> inetAddresses,
			PreBuiltTransportClientFactory preBuiltTransportClientFactory)
	{
		this.retryTemplate = Objects.requireNonNull(retryTemplate);
		this.clusterName = Objects.requireNonNull(clusterName);
		this.inetAddresses = Objects.requireNonNull(inetAddresses);
		if (inetAddresses.isEmpty())
		{
			throw new IllegalArgumentException("inetAddresses cannot be empty");
		}
		this.preBuiltTransportClientFactory = Objects.requireNonNull(preBuiltTransportClientFactory);
	}

	/**
	 * Tries to create a Client connecting to cluster on the given adresses.
	 *
	 * @throws InterruptedException if this thread gets interrupted while trying to connect
	 * @throws MolgenisDataException if maximum number of retries is exceeded
	 */
	Client createClient() throws InterruptedException
	{

		Client client = retryTemplate.execute(this::tryCreateClient);
		LOG.info("Connected to Elasticsearch cluster '{}'.", clusterName);
		return client;
	}

	private Client tryCreateClient(RetryContext retryContext) throws InterruptedException
	{
		if (Thread.interrupted())
		{
			throw new InterruptedException();
		}

		TransportClient result = preBuiltTransportClientFactory.build(clusterName, null)
															   .addTransportAddresses(createInetTransportAddresses());
		if (result.connectedNodes().isEmpty())
		{
			result.close();
			LOG.error("Failed to connect to Elasticsearch cluster '{}' on {}. Retry count = {}", clusterName,
					inetAddresses, retryContext.getRetryCount());
			throw new MolgenisDataException(
					String.format("Failed to connect to Elasticsearch cluster '%s' on %s. Is Elasticsearch running?",
							clusterName, inetAddresses));
		}
		return result;
	}

	private InetSocketTransportAddress[] createInetTransportAddresses()
	{
		return inetAddresses.stream().map(InetSocketTransportAddress::new).toArray(InetSocketTransportAddress[]::new);
	}
}
