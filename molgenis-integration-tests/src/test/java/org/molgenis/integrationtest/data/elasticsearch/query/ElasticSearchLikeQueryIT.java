package org.molgenis.integrationtest.data.elasticsearch.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractLikeQueryIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.query.ElasticSearchLikeQueryIT.LikeElasticSearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import java.text.ParseException;

@ContextConfiguration(classes = LikeElasticSearchTestConfig.class)
public class ElasticSearchLikeQueryIT extends AbstractLikeQueryIT
{
	@Configuration
	public static class LikeElasticSearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	// @Test Disabled, PostgreSql supports more datatypes for the like query than elastic search does
	// This results in MolgenisQueryException for ElasticSearch test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
