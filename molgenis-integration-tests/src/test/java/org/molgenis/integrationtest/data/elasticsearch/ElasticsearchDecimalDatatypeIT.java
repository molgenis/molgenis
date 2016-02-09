package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDecimalDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDecimalDatatypeIT.DecimalElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DecimalElasticsearchTestConfig.class)
public class ElasticsearchDecimalDatatypeIT extends AbstractDecimalDatatypeIT
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
