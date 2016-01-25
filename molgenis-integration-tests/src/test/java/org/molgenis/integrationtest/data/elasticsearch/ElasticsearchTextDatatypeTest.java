package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractTextDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchTextDatatypeTest.TextElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = TextElasticsearchTestConfig.class)
public class ElasticsearchTextDatatypeTest extends AbstractTextDatatypeTest
{
	@Configuration
	public static class TextElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
