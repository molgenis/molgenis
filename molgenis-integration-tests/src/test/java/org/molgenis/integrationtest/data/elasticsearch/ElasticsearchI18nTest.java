package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractI18nTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchI18nTest.I18nElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = I18nElasticsearchTestConfig.class)
public class ElasticsearchI18nTest extends AbstractI18nTest
{
	@Configuration
	public static class I18nElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Test
	@Override
	public void testLanguageService()
	{
		super.testLanguageService();
	}

	@Test
	@Override
	public void testMetaData()
	{
		super.testMetaData();
	}

}
