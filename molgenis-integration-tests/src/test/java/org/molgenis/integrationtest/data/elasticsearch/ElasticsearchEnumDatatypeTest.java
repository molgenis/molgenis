package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractEnumDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchEnumDatatypeTest.EnumElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = EnumElasticsearchTestConfig.class)
public class ElasticsearchEnumDatatypeTest extends AbstractEnumDatatypeTest
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
