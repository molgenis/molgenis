package org.molgenis.data.elasticsearch.client;

import com.evanlennick.retry4j.CallExecutor;
import com.evanlennick.retry4j.CallResults;
import com.evanlennick.retry4j.RetryConfig;
import com.evanlennick.retry4j.RetryConfigBuilder;
import com.evanlennick.retry4j.exception.Retry4jException;
import com.evanlennick.retry4j.exception.UnexpectedException;
import com.evanlennick.retry4j.listener.AfterFailedTryListener;
import com.evanlennick.retry4j.listener.BeforeNextTryListener;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import static java.lang.Math.min;
import static java.lang.Math.pow;

/**
 * Creates an Elasticsearch transport client based on given configuration settings.
 */
class ClientFactory
{
	private static final Logger LOG = LoggerFactory.getLogger(ClientFactory.class);
	private static final int MAX_CONNECTION_TRIES = 480; // Almost 24 hours when MAX_RETRY_WAIT is set to five minutes
	private static final Duration INITIAL_RETRY_WAIT = Duration.ofSeconds(1);
	private static final Duration MAX_RETRY_WAIT = Duration.ofMinutes(5);

	private ClientFactory()
	{
	}

	/**
	 * Tries to create a Client connecting to cluster on the given adresses.
	 * <p>
	 * In case the connection fail the connection is retried {@link #MAX_CONNECTION_TRIES} times.
	 * <p>
	 * Delay time is an exponential backoff starting with {@link #INITIAL_RETRY_WAIT}, with a maximum of
	 * {@link #MAX_RETRY_WAIT}.
	 * @param clusterName name of the cluster
	 * @param inetAddresses addresses to connect to
	 * @param preBuiltTransportClientFactory {@link PreBuiltTransportClientFactory} used to create the client
	 *
	 * @throws InterruptedException if this thread gets interrupted while trying to connect
	 * @throws MolgenisDataException if maximum number of retries is exceeded
	 */
	static Client createClient(String clusterName, List<InetSocketAddress> inetAddresses,
			PreBuiltTransportClientFactory preBuiltTransportClientFactory) throws InterruptedException
	{
		LOG.debug("Connecting to Elasticsearch cluster '{}' on {}...", clusterName, inetAddresses);
		try
		{
			CallResults results = createCallExecutor().execute(
					() -> tryCreateClient(clusterName, preBuiltTransportClientFactory, inetAddresses));
			LOG.info("Connected to Elasticsearch cluster '{}'.", clusterName);
			return (Client) results.getResult();
		}
		catch (UnexpectedException ex)
		{
			LOG.error("Failed to connect to Elasticsearch cluster.", ex);
			Throwable cause = ex.getCause();
			if (cause instanceof InterruptedException)
			{
				throw (InterruptedException) cause;
			}
			if (cause instanceof RuntimeException)
			{
				throw (RuntimeException) cause;
			}
			throw new MolgenisDataException(cause);
		}
		catch (Retry4jException ex)
		{
			LOG.error("Failed to connect to Elasticsearch cluster.", ex);
			throw new MolgenisDataException(ex);
		}
	}

	private static Client tryCreateClient(String clusterName,
			PreBuiltTransportClientFactory preBuiltTransportClientFactory, List<InetSocketAddress> inetAddresses)
			throws InterruptedException
	{
		if (Thread.interrupted())
		{
			throw new InterruptedException();
		}
		TransportClient result = preBuiltTransportClientFactory.build(clusterName, null)
															   .addTransportAddresses(
																	   createInetTransportAddresses(inetAddresses));
		if (result.connectedNodes().isEmpty())
		{
			result.close();
			throw new MolgenisDataException(
					String.format("Failed to connect to Elasticsearch cluster '%s' on %s. Is Elasticsearch running?",
							clusterName, inetAddresses));
		}
		return result;
	}

	private static CallExecutor createCallExecutor()
	{
		RetryConfig config = new RetryConfigBuilder().retryOnSpecificExceptions(MolgenisDataException.class)
													 .withMaxNumberOfTries(MAX_CONNECTION_TRIES)
													 .withDelayBetweenTries(INITIAL_RETRY_WAIT)
													 .withBackoffStrategy(ClientFactory::getSleepTime)
													 .build();
		CallExecutor callExecutor = new CallExecutor(config);
		callExecutor.registerRetryListener(
				(AfterFailedTryListener) callResults -> LOG.info("{} (Try {}). Sleeping before next try...",
						callResults.getLastExceptionThatCausedRetry().getMessage(), callResults.getTotalTries()));
		callExecutor.registerRetryListener((BeforeNextTryListener) callResults -> LOG.info("Retrying to connect..."));
		return callExecutor;
	}

	static long getSleepTime(int numberOfTriesFailed, Duration delayBetweenAttempts)
	{
		return (long) min(delayBetweenAttempts.toMillis() * pow(2.0, numberOfTriesFailed - 1.0),
				MAX_RETRY_WAIT.toMillis());
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
