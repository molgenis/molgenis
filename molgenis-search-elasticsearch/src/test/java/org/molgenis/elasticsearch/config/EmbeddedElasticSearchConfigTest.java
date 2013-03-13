package org.molgenis.elasticsearch.config;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.molgenis.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.search.SearchServiceFactory;
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
	public void searchServiceFactory()
	{
		SearchServiceFactory factory = context.getBean(SearchServiceFactory.class);
		assertNotNull(factory);
		assertTrue(factory instanceof EmbeddedElasticSearchServiceFactory);
	}
}
