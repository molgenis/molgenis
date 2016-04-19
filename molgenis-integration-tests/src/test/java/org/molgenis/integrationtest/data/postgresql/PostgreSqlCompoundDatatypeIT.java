package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractCompoundDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlCompoundDatatypeIT.CompoundPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = CompoundPostgreSqlTestConfig.class)
public class PostgreSqlCompoundDatatypeIT extends AbstractCompoundDatatypeIT
{
	@Configuration
	public static class CompoundPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
