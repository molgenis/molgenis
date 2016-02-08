package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractEnumDatatypeIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchEnumDatatypeIT.EnumElasticsearchTestConfig;
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
