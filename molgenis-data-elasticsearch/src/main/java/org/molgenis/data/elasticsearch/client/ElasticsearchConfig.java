package org.molgenis.data.elasticsearch.client;

import org.molgenis.data.index.IndexConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetSocketAddress;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Spring config for Elasticsearch server. Use this in your own app by importing this in your spring config:
 * <code> @Import(ElasticsearchConfig.class)</code>
 *
 * @author erwin
 */
@Configuration
@EnableScheduling
@Import({ IndexConfig.class, ConnectionRetryConfig.class })
public class ElasticsearchConfig
{
	@Value("${elasticsearch.cluster.name:molgenis}")
	private String clusterName;

	@Value("${elasticsearch.transport.addresses:127.0.0.1:9300}")
	private List<String> transportAddresses;

	final RetryTemplate retryTemplate;

	@Autowired
	public ElasticsearchConfig(RetryTemplate retryTemplate)
	{
		this.retryTemplate = retryTemplate;
	}

	@Bean(destroyMethod = "close")
	public ClientFacade elasticsearchClientFacade() throws InterruptedException
	{
		return new ClientFacade(clientFactory().createClient());
	}

	@Bean
	public ClientFactory clientFactory()
	{
		if (clusterName == null)
		{
			throw new IllegalArgumentException("Property 'elasticsearch.cluster.name' cannot be null");
		}
		if (transportAddresses == null || transportAddresses.isEmpty())
		{
			throw new IllegalArgumentException("Property 'elasticsearch.transport.addresses' cannot be null or empty");
		}
		return new ClientFactory(retryTemplate, clusterName, toIpSocketAddresses(transportAddresses),
				new PreBuiltTransportClientFactory());
	}

	private List<InetSocketAddress> toIpSocketAddresses(List<String> elasticsearchTransportAddresses)
	{
		return elasticsearchTransportAddresses.stream().map(this::toIpSocketAddress).collect(toList());
	}

	private InetSocketAddress toIpSocketAddress(String elasticsearchTransportAddress)
	{
		int idx = elasticsearchTransportAddress.lastIndexOf(':');
		if (idx == -1 || idx == elasticsearchTransportAddress.length() - 1)
		{
			throw new IllegalArgumentException(
					format("Invalid transport address '%s' in property elasticsearch.transport.addresses. Transport address should be of form 'hostname:port'",
							elasticsearchTransportAddress));
		}
		String hostname = elasticsearchTransportAddress.substring(0, idx);
		int port;
		try
		{
			port = Integer.parseInt(elasticsearchTransportAddress.substring(idx + 1));
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(
					format("Invalid transport address '%s' in property elasticsearch.transport.addresses. Transport address should be of form 'hostname:port'",
							elasticsearchTransportAddress));
		}
		return new InetSocketAddress(hostname, port);
	}
}
