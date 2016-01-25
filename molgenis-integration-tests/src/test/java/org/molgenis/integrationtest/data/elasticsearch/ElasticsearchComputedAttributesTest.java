package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractComputedAttributesTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchComputedAttributesTest.ComputedAttributesElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ComputedAttributesElasticsearchTestConfig.class)
public class ElasticsearchComputedAttributesTest extends AbstractComputedAttributesTest
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
