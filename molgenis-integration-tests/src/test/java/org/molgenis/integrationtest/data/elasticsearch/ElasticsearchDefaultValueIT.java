package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDefaultValueIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDefaultValueIT.DefaultValueElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DefaultValueElasticsearchTestConfig.class)
public class ElasticsearchDefaultValueIT extends AbstractDefaultValueIT
{
	@Configuration
	public static class DefaultValueElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
