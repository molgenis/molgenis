package org.molgenis.elasticsearch.config;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.elasticsearch.ElasticSearchService;
import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.search.SearchService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EmbeddedElasticSearchConfigTest
{
	private AnnotationConfigApplicationContext context;

	@BeforeClass
	public void beforeClass()
	{
		context = new AnnotationConfigApplicationContext(EmbeddedElasticSearchConfig.class);
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
