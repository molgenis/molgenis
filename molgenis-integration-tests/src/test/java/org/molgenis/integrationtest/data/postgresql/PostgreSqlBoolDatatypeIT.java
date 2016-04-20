package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractBoolDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlBoolDatatypeIT.BoolPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = BoolPostgreSqlTestConfig.class)
public class PostgreSqlBoolDatatypeIT extends AbstractBoolDatatypeIT
{
	@Configuration
	public static class BoolPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
