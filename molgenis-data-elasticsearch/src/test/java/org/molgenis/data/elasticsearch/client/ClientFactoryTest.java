package org.molgenis.data.elasticsearch.client;

import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ClientFactoryTest
{
	@Test
	public void testCreateClient() throws Exception
	{
		int port = 8032;
		String clusterName = "testCluster";

		PreBuiltTransportClientFactory preBuildClientFactory = mock(PreBuiltTransportClientFactory.class);
		PreBuiltTransportClient unConnectedClient = mock(PreBuiltTransportClient.class);
		PreBuiltTransportClient connectedClient = mock(PreBuiltTransportClient.class);
		DiscoveryNode node = mock(DiscoveryNode.class);
		when(connectedClient.connectedNodes()).thenReturn(Collections.singletonList(node));
		when(unConnectedClient.connectedNodes()).thenReturn(new ArrayList<>());
		when(connectedClient.addTransportAddresses(any(TransportAddress.class))).thenReturn(connectedClient);
		when(unConnectedClient.addTransportAddresses(any(TransportAddress.class))).thenReturn(unConnectedClient);
		when(preBuildClientFactory.build(clusterName, null))
				.thenReturn(unConnectedClient, unConnectedClient, connectedClient);

		Client client = ClientFactory.createClient(clusterName, Collections.singletonList(new InetSocketAddress(port)),
				preBuildClientFactory);

		assertEquals(client, connectedClient);
		verify(preBuildClientFactory, times(3)).build(clusterName, null);
	}

}