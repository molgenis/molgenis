package org.molgenis.integrationtest.data.elasticsearch.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractLikeQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticsearchLikeQueryIT.LikeElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = LikeElasticsearchTestConfig.class)
public class ElasticsearchLikeQueryIT extends AbstractLikeQueryIT
{
	@Configuration
	public static class LikeElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		createTestRepo();

		// Data types INT, DECIMAL, LONG, DATE, DATE_TIME, BOOL, XREF and MREF not supported for operator LIKE
		testString();
	}
}
