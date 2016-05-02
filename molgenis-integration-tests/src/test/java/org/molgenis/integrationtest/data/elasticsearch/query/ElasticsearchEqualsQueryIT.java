package org.molgenis.integrationtest.data.elasticsearch.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractEqualsQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchEqualsQueryIT.EqualsElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = EqualsElasticsearchTestConfig.class)
public class ElasticsearchEqualsQueryIT extends AbstractEqualsQueryIT
{
	@Configuration
	public static class EqualsElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
