package org.molgenis.integrationtest.data.elasticsearch;

import java.text.ParseException;

import org.molgenis.integrationtest.data.AbstractCountIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchCountIT.CountElasticsearchTestConfig;
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
