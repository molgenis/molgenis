package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractDecimalDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlDecimalDatatypeIT.DecimalPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DecimalPostgreSqlTestConfig.class)
public class PostgreSqlDecimalDatatypeIT extends AbstractDecimalDatatypeIT
{
	@Configuration
	public static class DecimalPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
