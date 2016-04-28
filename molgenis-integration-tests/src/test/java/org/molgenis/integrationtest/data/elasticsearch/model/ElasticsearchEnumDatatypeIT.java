package org.molgenis.integrationtest.data.elasticsearch.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractEnumDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.model.ElasticsearchEnumDatatypeIT.EnumElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = EnumElasticsearchTestConfig.class)
public class ElasticsearchEnumDatatypeIT extends AbstractEnumDatatypeIT
{
	@Configuration
	public static class EnumElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{

	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
