package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractI18nIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlI18nIT.I18nMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = I18nMySqlTestConfig.class)
public class MySqlI18nIT extends AbstractI18nIT
{
	@Configuration
	public static class I18nMySqlTestConfig extends AbstractMySqlTestConfig
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
