package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractCompoundDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchCompoundDatatypeTest.CompoundElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = CompoundElasticsearchTestConfig.class)
public class ElasticsearchCompoundDatatypeTest extends AbstractCompoundDatatypeTest
{
	@Configuration
	public static class CompoundElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
