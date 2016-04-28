package org.molgenis.integrationtest.data.elasticsearch.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractSearchQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticSearchSearchQueryIT.SearchElasticSearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = SearchElasticSearchTestConfig.class)
public class ElasticSearchSearchQueryIT extends AbstractSearchQueryIT
{
	@Configuration
	public static class SearchElasticSearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
