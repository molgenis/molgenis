package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDateDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDateDatatypeIT.DateElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DateElasticsearchTestConfig.class)
public class ElasticsearchDateDatatypeIT extends AbstractDateDatatypeIT
{
	@Configuration
	public static class DateElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
