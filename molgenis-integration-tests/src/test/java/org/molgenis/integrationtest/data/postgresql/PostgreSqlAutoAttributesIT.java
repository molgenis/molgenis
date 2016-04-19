package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractAutoAttributesIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlAutoAttributesIT.AutoAttributesPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AutoAttributesPostgreSqlTestConfig.class)
public class PostgreSqlAutoAttributesIT extends AbstractAutoAttributesIT
{
	@Configuration
	public static class AutoAttributesPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
