package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.*;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.elasticsearch.client.ClientFactory.getSleepTime;
import static org.testng.Assert.*;

public class ClientFactoryTest extends AbstractMockitoTest
{
	@Mock
	PreBuiltTransportClientFactory preBuildClientFactory;
	@Mock
	PreBuiltTransportClient unConnectedClient;
	@Mock
	PreBuiltTransportClient connectedClient;
	@Mock
	DiscoveryNode node;
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	@BeforeMethod

	public void beforeMethod()
	{
		when(connectedClient.addTransportAddresses(any(TransportAddress.class))).thenReturn(connectedClient);
		when(unConnectedClient.addTransportAddresses(any(TransportAddress.class))).thenReturn(unConnectedClient);

		when(connectedClient.connectedNodes()).thenReturn(singletonList(node));
		when(unConnectedClient.connectedNodes()).thenReturn(emptyList());
	}

	@Test
	public void testCreateClient() throws Exception
	{
		int port = 8032;
		String clusterName = "testCluster";

		when(preBuildClientFactory.build(clusterName, null)).thenReturn(unConnectedClient, unConnectedClient,
				connectedClient);

		Client client = ClientFactory.createClient(clusterName, singletonList(new InetSocketAddress(port)),
				preBuildClientFactory);

		assertEquals(client, connectedClient);
		verify(preBuildClientFactory, times(3)).build(clusterName, null);
		verify(unConnectedClient, times(2)).close();
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testCreateClientNullAddresses() throws Exception
	{
		ClientFactory.createClient("testCluster", null, preBuildClientFactory);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCreateClientEmptyAddresses() throws Exception
	{
		ClientFactory.createClient("testCluster", emptyList(), preBuildClientFactory);
	}

	@Test
	public void testCreateClientInterrupted() throws InterruptedException, ExecutionException
	{
		int port = 8032;
		String clusterName = "testCluster";

		when(preBuildClientFactory.build(clusterName, null)).thenReturn(unConnectedClient);

		Future<Client> result = executorService.submit(
				() -> ClientFactory.createClient(clusterName, singletonList(new InetSocketAddress(port)),
						preBuildClientFactory));

		result.cancel(true);
		try
		{
			result.get();
			fail("Should throw cancellation exception!");
		}
		catch (CancellationException expected)
		{
		}
		Thread.sleep(2000);
		verify(preBuildClientFactory, atMost(1)).build(any(), any());
	}

	@Test
	public void testSleepTimeMaxesOutOnFiveMinutes()
	{
		assertEquals(getSleepTime(480, ofSeconds(1)), ofMinutes(5).toMillis());
	}

	@Test
	public void testSleepTimeDecays()
	{
		long firstSleepTime = getSleepTime(1, ofSeconds(1));
		long secondSleepTime = getSleepTime(2, ofSeconds(1));

		assertTrue(firstSleepTime < secondSleepTime);
	}

	@Test
	public void testFirstSleepTimeEqualsOneSecond()
	{
		assertEquals(getSleepTime(1, ofSeconds(1)), 1000);
	}

}