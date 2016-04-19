package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractI18nIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlI18nIT.I18nPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = I18nPostgreSqlTestConfig.class)
public class PostgreSqlI18nIT extends AbstractI18nIT
{
	@Configuration
	public static class I18nPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
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
