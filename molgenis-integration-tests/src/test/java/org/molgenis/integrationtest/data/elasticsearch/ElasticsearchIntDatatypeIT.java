package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractIntDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchIntDatatypeIT.IntElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = IntElasticsearchTestConfig.class)
public class ElasticsearchIntDatatypeIT extends AbstractIntDatatypeIT
{
	@Configuration
	public static class IntElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
