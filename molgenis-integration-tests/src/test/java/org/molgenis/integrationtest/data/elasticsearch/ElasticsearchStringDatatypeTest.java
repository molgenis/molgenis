package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractStringDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchStringDatatypeTest.StringElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = StringElasticsearchTestConfig.class)
public class ElasticsearchStringDatatypeTest extends AbstractStringDatatypeTest
{
	@Configuration
	public static class StringElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
