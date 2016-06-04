package org.molgenis.data.elasticsearch.config;

import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.slf4j.Slf4jESLoggerFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.ElasticsearchEntityFactory;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.IndexConfig;
import org.molgenis.mail.MailConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.Collections;

/**
 * Spring config for embedded elastic search server. Use this in your own app by importing this in your spring config:
 * <code> @Import(EmbeddedElasticSearchConfig.class)</code>
 *
 * @author erwin
 */
@Configuration
@EnableScheduling
@Import({ IndexConfig.class, MailConfig.class })
public class EmbeddedElasticSearchConfig
{
	static
	{
		// force Elasticsearch to use slf4j instead of default log4j logging
		ESLoggerFactory.setDefaultFactory(new Slf4jESLoggerFactory());
	}

	@Autowired
	private DataService dataService;

	@Autowired
	private ElasticsearchEntityFactory elasticsearchEntityFactory;

	@Bean(destroyMethod = "close")
	public EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory()
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

		return new EmbeddedElasticSearchServiceFactory(Collections.singletonMap("path.data", molgenisDataDirStr));
	}

	@Bean
	public SearchService searchService()
	{
		return embeddedElasticSearchServiceFactory().create(dataService, elasticsearchEntityFactory);
	}
}
