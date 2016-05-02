package org.molgenis.integrationtest.data.elasticsearch.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractAndQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchAndQueryIT.AndElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = AndElasticsearchTestConfig.class)
public class ElasticsearchAndQueryIT extends AbstractAndQueryIT
{
	@Configuration
	public static class AndElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}

}
