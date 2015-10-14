package org.molgenis.data.elasticsearch.config;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.elasticsearch.ElasticSearchService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.transaction.MolgenisTransactionManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EmbeddedElasticSearchConfigTest
{
	private AnnotationConfigApplicationContext context;

	@BeforeMethod
	public void beforeClass()
	{
		System.setProperty("molgenis.home", System.getProperty("java.io.tmpdir"));
		context = new AnnotationConfigApplicationContext(DataServiceImpl.class, EmbeddedElasticSearchConfig.class,
				EntityToSourceConverter.class, Config.class);
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MolgenisTransactionManager molgenisTransactionManager()
		{
			return mock(MolgenisTransactionManager.class);
		}
	}

	@Test
	public void embeddedElasticSearchServiceFactory()
	{
		EmbeddedElasticSearchServiceFactory factory = context.getBean(EmbeddedElasticSearchServiceFactory.class);
		assertNotNull(factory);
	}

	public void searchService()
	{
		SearchService searchService = context.getBean(SearchService.class);
		assertNotNull(searchService);
		assertTrue(searchService instanceof ElasticSearchService);
	}
}
