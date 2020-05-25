package org.molgenis.data.elasticsearch.client;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.molgenis.data.MolgenisDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

/** Creates an Elasticsearch REST client based on given configuration settings. */
class ClientFactory {
  private static final Logger LOG = LoggerFactory.getLogger(ClientFactory.class);

  private final RetryTemplate retryTemplate;
  private final RestHighLevelClient client;
  private final String hostnames;

  /**
   * @param hosts addresses to connect to
   * @param retryTemplate {@link RetryTemplate} to keep trying to connect
   */
  public ClientFactory(RetryTemplate retryTemplate, List<HttpHost> hosts) {
    this.retryTemplate = requireNonNull(retryTemplate);
    requireNonNull(hosts);
    if (hosts.isEmpty()) {
      throw new IllegalArgumentException("hosts cannot be empty");
    }
    hostnames = hosts.stream().map(HttpHost::toURI).collect(Collectors.joining());
    client = new RestHighLevelClient(RestClient.builder(hosts.toArray(new HttpHost[] {})));
  }

  /**
   * Tries to create a Client connecting to cluster on the given adresses.
   *
   * @throws InterruptedException if this thread gets interrupted while trying to connect
   * @throws MolgenisDataException if maximum number of retries is exceeded
   */
  RestHighLevelClient createClient() throws InterruptedException {
    retryTemplate.execute(context -> ClientFactory.tryPing(client, hostnames, context));
    LOG.info("Connected to Elasticsearch cluster.");
    return client;
  }

  static Void tryPing(RestHighLevelClient client, String hostnames, RetryContext retryContext)
      throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }
    try {
      if (!client.ping(RequestOptions.DEFAULT)) {
        LOG.error(
            "Failed to connect to Elasticsearch cluster on {}. Retry count = {}",
            hostnames,
            retryContext.getRetryCount());
        throw new MolgenisDataException(
            String.format(
                "Failed to connect to Elasticsearch cluster on %s. Is Elasticsearch running?",
                hostnames));
      }
    } catch (IOException e) {
      LOG.error(
          "Failed to connect to Elasticsearch cluster on {}. Retry count = {}",
          hostnames,
          retryContext.getRetryCount());
      throw new MolgenisDataException(
          String.format(
              "Failed to connect to Elasticsearch cluster on %s. Is Elasticsearch running?",
              hostnames));
    }
    return null;
  }
}
