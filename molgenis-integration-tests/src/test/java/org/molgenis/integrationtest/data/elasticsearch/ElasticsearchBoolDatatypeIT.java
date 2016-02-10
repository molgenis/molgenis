package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractBoolDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchBoolDatatypeIT.BoolElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = BoolElasticsearchTestConfig.class)
public class ElasticsearchBoolDatatypeIT extends AbstractBoolDatatypeIT
{
	@Configuration
	public static class BoolElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
