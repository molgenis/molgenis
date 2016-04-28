package org.molgenis.integrationtest.data.elasticsearch.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractCountIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchCountIT.CountElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = CountElasticsearchTestConfig.class)
public class ElasticsearchCountIT extends AbstractCountIT
{
	@Configuration
	public static class CountElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
