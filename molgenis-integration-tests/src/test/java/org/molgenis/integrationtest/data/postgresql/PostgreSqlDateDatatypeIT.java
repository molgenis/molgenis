package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractDateDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlDateDatatypeIT.DatePostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DatePostgreSqlTestConfig.class)
public class PostgreSqlDateDatatypeIT extends AbstractDateDatatypeIT
{
	@Configuration
	public static class DatePostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
