package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractIntDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlIntDatatypeIT.IntPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = IntPostgreSqlTestConfig.class)
public class PostgreSqlIntDatatypeIT extends AbstractIntDatatypeIT
{
	@Configuration
	public static class IntPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
