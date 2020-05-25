package org.molgenis.data.elasticsearch.client;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.List;
import org.apache.http.HttpHost;
import org.molgenis.data.index.IndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring config for Elasticsearch server. Use this in your own app by importing this in your spring
 * config: <code> @Import(ElasticsearchConfig.class)</code>
 *
 * @author erwin
 */
@Configuration
@EnableScheduling
@Import({IndexConfig.class, ConnectionRetryConfig.class})
public class ElasticsearchConfig {

  @Value("${elasticsearch.hosts:127.0.0.1:9200}")
  private List<String> hosts;

  final RetryTemplate retryTemplate;

  @Autowired
  public ElasticsearchConfig(RetryTemplate retryTemplate) {
    this.retryTemplate = retryTemplate;
  }

  @Bean(destroyMethod = "close")
  public ClientFacade elasticsearchClientFacade() throws InterruptedException {
    return new ClientFacade(clientFactory().createClient());
  }

  @Bean
  public ClientFactory clientFactory() {
    if (hosts == null || hosts.isEmpty()) {
      throw new IllegalArgumentException("Property 'elasticsearch.hosts' cannot be null or empty");
    }
    return new ClientFactory(retryTemplate, toHosts(hosts));
  }

  private List<HttpHost> toHosts(List<String> addressses) {
    return addressses.stream().map(this::toHost).collect(toList());
  }

  private HttpHost toHost(String address) {
    int idx = address.lastIndexOf(':');
    if (idx == -1 || idx == address.length() - 1) {
      throw new IllegalArgumentException(
          format(
              "Invalid hostname '%s' in property elasticsearch.hosts. Host should be of form 'hostname:port'",
              address));
    }
    String hostname = address.substring(0, idx);
    int port;
    try {
      port = Integer.parseInt(address.substring(idx + 1));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          format(
              "Invalid hostname '%s' in property elasticsearch.hosts. Host should be of form 'hostname:port'",
              address));
    }
    return new HttpHost(hostname, port);
  }
}
