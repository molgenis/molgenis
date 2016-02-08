package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDatetimeDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDatetimeDatatypeIT.DatetimeElasticsearchTestConfig;
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
