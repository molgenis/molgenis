package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractFileDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchFileDatatypeIT.FileElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = FileElasticsearchTestConfig.class)
public class ElasticsearchFileDatatypeIT extends AbstractFileDatatypeIT
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
