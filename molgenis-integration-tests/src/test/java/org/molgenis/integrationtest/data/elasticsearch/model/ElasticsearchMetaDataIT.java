package org.molgenis.integrationtest.data.elasticsearch.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractMetaDataIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.model.ElasticsearchMetaDataIT.MetaDataElasticsearchTestConfig;
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
		// super.testIt(); // see https://github.com/molgenis/molgenis/issues/4478
	}
}
