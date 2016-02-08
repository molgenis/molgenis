package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractStringDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchStringDatatypeIT.StringElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = StringElasticsearchTestConfig.class)
public class ElasticsearchStringDatatypeIT extends AbstractStringDatatypeIT
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
