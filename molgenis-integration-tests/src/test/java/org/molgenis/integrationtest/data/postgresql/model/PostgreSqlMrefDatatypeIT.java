package org.molgenis.integrationtest.data.postgresql.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractMrefDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = PostgreSqlMrefDatatypeIT.MrefPostgreSqlTestConfig.class)
public class PostgreSqlMrefDatatypeIT extends AbstractMrefDatatypeIT
{
	@Configuration
	public static class MrefPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
