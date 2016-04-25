package org.molgenis.integrationtest.data.elasticsearch.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractDatetimeDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.model.ElasticsearchDatetimeDatatypeIT.DatetimeElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DatetimeElasticsearchTestConfig.class)
public class ElasticsearchDatetimeDatatypeIT extends AbstractDatetimeDatatypeIT
{
	@Configuration
	public static class DatetimeElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
