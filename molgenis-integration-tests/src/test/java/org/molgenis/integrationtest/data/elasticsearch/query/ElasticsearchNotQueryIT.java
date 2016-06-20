package org.molgenis.integrationtest.data.elasticsearch.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractNotQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchNotQueryIT.NotElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = NotElasticsearchTestConfig.class)
public class ElasticsearchNotQueryIT extends AbstractNotQueryIT
{
	@Configuration
	public static class NotElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}


