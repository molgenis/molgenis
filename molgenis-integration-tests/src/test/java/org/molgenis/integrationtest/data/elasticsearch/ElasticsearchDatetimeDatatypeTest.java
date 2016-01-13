package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDatetimeDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDatetimeDatatypeTest.DatetimeElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DatetimeElasticsearchTestConfig.class)
public class ElasticsearchDatetimeDatatypeTest extends AbstractDatetimeDatatypeTest
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
