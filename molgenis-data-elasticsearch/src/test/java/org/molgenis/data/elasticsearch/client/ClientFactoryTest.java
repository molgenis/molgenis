package org.molgenis.data.elasticsearch.client;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

@ExtendWith(MockitoExtension.class)
class ClientFactoryTest extends AbstractMockitoTest {
  @Mock private RetryTemplate retryTemplate;
  @Mock private RestHighLevelClient client;
  @Mock private RetryContext retryContext;

  private ClientFactory clientFactory;

  @BeforeEach
  void setup() {
    clientFactory =
        new ClientFactory(retryTemplate, singletonList(new HttpHost("localhost", 8032)));
  }

  @Test
  void testCreateClientFails() throws Throwable {
    doThrow(new MolgenisDataException("Failed")).when(retryTemplate).execute(any());
    assertThrows(MolgenisDataException.class, () -> clientFactory.createClient());
  }

  @Test
  void testCreateClientSuccess() throws Throwable {
    when(retryTemplate.execute(any())).thenReturn(client);
    assertDoesNotThrow(() -> clientFactory.createClient());
  }

  @Test
  void testCreateClientNullAddresses() {
    assertThrows(NullPointerException.class, () -> new ClientFactory(retryTemplate, null));
  }

  @Test
  void testCreateClientEmptyAddresses() {
    assertThrows(
        IllegalArgumentException.class, () -> new ClientFactory(retryTemplate, emptyList()));
  }

  @Test
  void testTryPingSuccess() throws InterruptedException, IOException {
    when(client.ping(RequestOptions.DEFAULT)).thenReturn(true);

    assertDoesNotThrow(() -> ClientFactory.tryPing(client, "localhost:9022", retryContext));
  }

  @Test
  void testTryPingFail() throws InterruptedException, IOException {
    when(client.ping(RequestOptions.DEFAULT)).thenReturn(false);

    assertThrows(
        MolgenisDataException.class,
        () -> ClientFactory.tryPing(client, "localhost:9022", retryContext));
  }

  @Test
  void testTryPingThrown() throws IOException {
    doThrow(new IOException()).when(client).ping(RequestOptions.DEFAULT);

    assertThrows(
        MolgenisDataException.class,
        () -> ClientFactory.tryPing(client, "localhost:9022", retryContext));
  }
}
