package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractBoolDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchBoolDatatypeTest.BoolElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = BoolElasticsearchTestConfig.class)
public class ElasticsearchBoolDatatypeTest extends AbstractBoolDatatypeTest
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
