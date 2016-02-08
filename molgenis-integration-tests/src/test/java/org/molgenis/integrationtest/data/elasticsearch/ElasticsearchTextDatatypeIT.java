package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractTextDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchTextDatatypeIT.TextElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = TextElasticsearchTestConfig.class)
public class ElasticsearchTextDatatypeIT extends AbstractTextDatatypeIT
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
