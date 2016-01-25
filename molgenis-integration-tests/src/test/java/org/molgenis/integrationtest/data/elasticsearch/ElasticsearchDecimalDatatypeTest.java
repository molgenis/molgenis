package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDecimalDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDecimalDatatypeTest.DecimalElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DecimalElasticsearchTestConfig.class)
public class ElasticsearchDecimalDatatypeTest extends AbstractDecimalDatatypeTest
{
	@Configuration
	public static class DecimalElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
