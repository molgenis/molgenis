package org.molgenis.integrationtest.data.elasticsearch.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractLikeQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticSearchLikeQueryIT.LikeElasticSearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = LikeElasticSearchTestConfig.class)
public class ElasticSearchLikeQueryIT extends AbstractLikeQueryIT
{
	@Configuration
	public static class LikeElasticSearchTestConfig extends AbstractElasticsearchTestConfig
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
