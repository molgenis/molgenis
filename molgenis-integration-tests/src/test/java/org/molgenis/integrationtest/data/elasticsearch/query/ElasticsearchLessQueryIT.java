package org.molgenis.integrationtest.data.elasticsearch.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractLessQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchLessQueryIT.LessElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = LessElasticsearchTestConfig.class)
public class ElasticsearchLessQueryIT extends AbstractLessQueryIT
{
	@Configuration
	public static class LessElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
