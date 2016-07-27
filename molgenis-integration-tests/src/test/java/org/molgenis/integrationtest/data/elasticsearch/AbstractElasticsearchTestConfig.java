package org.molgenis.integrationtest.data.elasticsearch;

import java.io.IOException;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.molgenis.data.ManageableRepositoryCollection;
import org.molgenis.data.elasticsearch.ElasticsearchRepositoryCollection;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.molgenis.integrationtest.data.AbstractDataApiTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSender;
import org.testng.annotations.AfterClass;

//@Import({EmbeddedElasticSearchConfig.class, ElasticsearchEntityFactory.class, ElasticsearchRepositoryCollection.class})
import static org.mockito.Mockito.mock;

public abstract class AbstractElasticsearchTestConfig extends AbstractDataApiTestConfig
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	ReindexActionRegisterService reindexActionRegisterService;

	@Autowired
	protected SearchService searchService;

	@Autowired
	protected DataSource dataSource;

	@Override
	protected ManageableRepositoryCollection getBackend()
	{
		return elasticsearchRepositoryCollection();
	}

	@Bean
	public ElasticsearchRepositoryCollection elasticsearchRepositoryCollection()
	{
		return new ElasticsearchRepositoryCollection(searchService, dataService());
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties()
	{
		PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
		Resource[] resources = new Resource[]
				{ new FileSystemResource(System.getProperty("molgenis.home") + "/molgenis-server.properties"),
						new ClassPathResource("/elasticsearch/molgenis.properties") };
		pspc.setLocations(resources);
		pspc.setFileEncoding("UTF-8");
		pspc.setIgnoreUnresolvablePlaceholders(true);
		pspc.setIgnoreResourceNotFound(true);
		pspc.setNullValue("@null");
		return pspc;
	}

	@AfterClass
	public void cleanUp()
	{
		try
		{
			// Stop ES
			embeddedElasticSearchServiceFactory.close();
		}
		catch (IOException e)
		{
			logger.error("Error stopping Elasticsearch", e);
		}
	}

	@Override
	public void setUp(){
		//nothing to do for now
	}
}
