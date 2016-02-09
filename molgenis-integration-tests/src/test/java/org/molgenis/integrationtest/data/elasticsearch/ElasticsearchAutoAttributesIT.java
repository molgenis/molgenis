package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractAutoAttributesIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchAutoAttributesIT.AutoAttributesElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AutoAttributesElasticsearchTestConfig.class)
public class ElasticsearchAutoAttributesIT extends AbstractAutoAttributesIT
{
	@Configuration
	public static class AutoAttributesElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
