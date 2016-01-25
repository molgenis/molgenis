package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractMrefDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchMrefDatatypeTest.MrefElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MrefElasticsearchTestConfig.class)
public class ElasticsearchMrefDatatypeTest extends AbstractMrefDatatypeTest
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
