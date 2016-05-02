package org.molgenis.integrationtest.data.elasticsearch.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractOrQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchOrQueryIT.OrElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = OrElasticsearchTestConfig.class)
public class ElasticsearchOrQueryIT extends AbstractOrQueryIT
{
	@Configuration
	public static class OrElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}