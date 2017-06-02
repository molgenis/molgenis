package org.molgenis.data.elasticsearch.config;

import com.google.common.collect.Maps;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.factory.ElasticsearchServiceFactory;
import org.molgenis.data.elasticsearch.index.IndexConfig;
import org.molgenis.data.elasticsearch.util.DocumentIdGenerator;
import org.molgenis.data.index.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.Map;

/**
 * Spring config for Elasticsearch server. Use this in your own app by importing this in your spring config:
 * <code> @Import(ElasticsearchConfig.class)</code>
 *
 * @author erwin
 */
@Configuration
@EnableScheduling
@Import({ IndexConfig.class })
public class ElasticsearchConfig
{
	@Value("${elasticsearch.transport.tcp.port:@null}")
	private String elasticsearchTransportTcpPort;

	@Autowired
	private DataService dataService;

	@Autowired
	private ElasticsearchEntityFactory elasticsearchEntityFactory;

	@Autowired
	private DocumentIdGenerator documentIdGenerator;

	@Bean(destroyMethod = "close")
	public ElasticsearchServiceFactory elasticsearchServiceFactory()
	{
		// get molgenis home directory
		String molgenisHomeDir = System.getProperty("molgenis.home");
		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}
		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';

		// create molgenis data directory if not exists
		String molgenisDataDirStr = molgenisHomeDir + "data";
		File molgenisDataDir = new File(molgenisDataDirStr);
		if (!molgenisDataDir.exists())
		{
			if (!molgenisDataDir.mkdir())
			{
				throw new RuntimeException("failed to create directory: " + molgenisDataDirStr);
			}
		}

		Map<String, String> providedSettings = Maps.newHashMapWithExpectedSize(2);
		providedSettings.put("path.data", molgenisDataDirStr);
		providedSettings.put("cluster.name", "molgenis");
		if (elasticsearchTransportTcpPort != null)
		{
			providedSettings.put("transport.tcp.port", elasticsearchTransportTcpPort);
		}
		return new ElasticsearchServiceFactory(providedSettings);
	}

	@Bean
	public SearchService searchService()
	{
		return elasticsearchServiceFactory().create(dataService, elasticsearchEntityFactory, documentIdGenerator);
	}
}
