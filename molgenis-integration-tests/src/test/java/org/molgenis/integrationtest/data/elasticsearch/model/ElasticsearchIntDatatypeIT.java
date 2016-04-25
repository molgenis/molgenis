package org.molgenis.integrationtest.data.elasticsearch.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractIntDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.model.ElasticsearchIntDatatypeIT.IntElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = IntElasticsearchTestConfig.class)
public class ElasticsearchIntDatatypeIT extends AbstractIntDatatypeIT
{
	@Configuration
	public static class IntElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
