package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractMetaDataIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchMetaDataIT.MetaDataElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MetaDataElasticsearchTestConfig.class)
public class ElasticsearchMetaDataIT extends AbstractMetaDataIT
{
	@Configuration
	public static class MetaDataElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
