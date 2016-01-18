package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractExtendsTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchExtendsTest.ExtendsElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ExtendsElasticsearchTestConfig.class)
public class ElasticsearchExtendsTest extends AbstractExtendsTest
{
	@Configuration
	public static class ExtendsElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
