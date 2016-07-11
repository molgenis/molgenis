package org.molgenis.integrationtest.data.elasticsearch.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractNestedQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchNestedQueryIT.NestedElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = NestedElasticsearchTestConfig.class)
public class ElasticsearchNestedQueryIT extends AbstractNestedQueryIT
{
	@Configuration
	public static class NestedElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
