package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractEnumDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlEnumDatatypeIT.EnumPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = EnumPostgreSqlTestConfig.class)
public class PostgreSqlEnumDatatypeIT extends AbstractEnumDatatypeIT
{
	@Configuration
	public static class EnumPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
