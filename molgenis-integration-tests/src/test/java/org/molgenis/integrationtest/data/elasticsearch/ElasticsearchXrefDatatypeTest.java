package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractXrefDatatypeTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchXrefDatatypeTest.XrefElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = XrefElasticsearchTestConfig.class)
public class ElasticsearchXrefDatatypeTest extends AbstractXrefDatatypeTest
{
	@Configuration
	public static class XrefElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
