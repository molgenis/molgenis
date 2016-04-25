package org.molgenis.integrationtest.data.postgresql.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractDatetimeDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.model.PostgreSqlDatetimeDatatypeIT.DatetimePostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DatetimePostgreSqlTestConfig.class)
public class PostgreSqlDatetimeDatatypeIT extends AbstractDatetimeDatatypeIT
{
	@Configuration
	public static class DatetimePostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
