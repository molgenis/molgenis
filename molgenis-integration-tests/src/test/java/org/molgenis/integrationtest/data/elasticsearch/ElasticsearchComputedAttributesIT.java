package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractComputedAttributesIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchComputedAttributesIT.ComputedAttributesElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ComputedAttributesElasticsearchTestConfig.class)
public class ElasticsearchComputedAttributesIT extends AbstractComputedAttributesIT
{
	@Configuration
	public static class ComputedAttributesElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
