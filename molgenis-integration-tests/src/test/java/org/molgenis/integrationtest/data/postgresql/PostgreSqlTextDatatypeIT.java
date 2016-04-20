package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractTextDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlTextDatatypeIT.TextPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = TextPostgreSqlTestConfig.class)
public class PostgreSqlTextDatatypeIT extends AbstractTextDatatypeIT
{
	@Configuration
	public static class TextPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
