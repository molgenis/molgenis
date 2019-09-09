package org.molgenis.data.elasticsearch.client;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.retry.support.RetryTemplate;

class ClientFactoryTest extends AbstractMockitoTest {
  @Mock private PreBuiltTransportClientFactory preBuildClientFactory;
  @Mock private PreBuiltTransportClient unConnectedClient;
  @Mock private PreBuiltTransportClient connectedClient;
  @Mock private DiscoveryNode node;
  private RetryTemplate retryTemplate = new RetryTemplate();

  @Test
  void testCreateClient() throws Exception {
    initMockClient();
    int port = 8032;
    String clusterName = "testCluster";

    when(preBuildClientFactory.build(clusterName, null))
        .thenReturn(unConnectedClient, unConnectedClient, connectedClient);

    ClientFactory clientFactory =
        new ClientFactory(
            retryTemplate,
            clusterName,
            singletonList(new InetSocketAddress(port)),
            preBuildClientFactory);
    Client client = clientFactory.createClient();

    assertEquals(client, connectedClient);
    verify(preBuildClientFactory, times(3)).build(clusterName, null);
    verify(unConnectedClient, times(2)).close();
  }

  @Test
  void testCreateClientNullAddresses() {
    assertThrows(
        NullPointerException.class,
        () -> new ClientFactory(retryTemplate, "testCluster", null, preBuildClientFactory));
  }

  @Test
  void testCreateClientEmptyAddresses() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ClientFactory(retryTemplate, "testCluster", emptyList(), preBuildClientFactory));
  }

  private void initMockClient() {
    when(connectedClient.addTransportAddresses(any(TransportAddress.class)))
        .thenReturn(connectedClient);
    when(unConnectedClient.addTransportAddresses(any(TransportAddress.class)))
        .thenReturn(unConnectedClient);

    when(connectedClient.connectedNodes()).thenReturn(singletonList(node));
    when(unConnectedClient.connectedNodes()).thenReturn(emptyList());
  }
}
