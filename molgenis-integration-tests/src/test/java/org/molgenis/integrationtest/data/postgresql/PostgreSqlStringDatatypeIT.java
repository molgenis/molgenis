package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractStringDatatypeIT;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = PostgreSqlStringDatatypeIT.StringPostgreSqlTestConfig.class)
public class PostgreSqlStringDatatypeIT extends AbstractStringDatatypeIT
{
	@Configuration
	public static class StringPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
