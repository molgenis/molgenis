package org.molgenis.integrationtest.data.elasticsearch.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractNotQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticSearchNotQueryIT.NotElasticSearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = NotElasticSearchTestConfig.class)
public class ElasticSearchNotQueryIT extends AbstractNotQueryIT
{
	@Configuration
	public static class NotElasticSearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}


