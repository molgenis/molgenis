package org.molgenis.integrationtest.data.elasticsearch.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractDecimalDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.model.ElasticsearchDecimalDatatypeIT.DecimalElasticsearchTestConfig;
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
