package org.molgenis.elasticsearch.config;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.search.SearchService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EmbeddedElasticSearchConfigTest
{
	private AnnotationConfigApplicationContext context;

	@BeforeMethod
	public void beforeClass()
	{
		System.setProperty("molgenis.home", System.getProperty("java.io.tmpdir"));
		context = new AnnotationConfigApplicationContext(DataServiceImpl.class, EmbeddedElasticSearchConfig.class);
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
