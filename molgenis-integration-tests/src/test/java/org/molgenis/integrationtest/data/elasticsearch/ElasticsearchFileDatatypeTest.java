package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractFileDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchFileDatatypeTest.FileElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = FileElasticsearchTestConfig.class)
public class ElasticsearchFileDatatypeTest extends AbstractFileDatatypeTest
{
	@Configuration
	public static class FileElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
