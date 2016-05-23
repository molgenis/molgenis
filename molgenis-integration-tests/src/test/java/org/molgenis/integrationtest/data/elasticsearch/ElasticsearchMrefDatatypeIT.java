package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractMrefDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchMrefDatatypeIT.MrefElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MrefElasticsearchTestConfig.class)
public class ElasticsearchMrefDatatypeIT extends AbstractMrefDatatypeIT
{
	@Configuration
	public static class MrefElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
