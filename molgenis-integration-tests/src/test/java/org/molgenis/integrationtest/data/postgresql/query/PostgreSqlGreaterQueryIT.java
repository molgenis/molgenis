package org.molgenis.integrationtest.data.postgresql.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractGreaterQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlGreaterQueryIT.GreaterPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = GreaterPostgreSqlTestConfig.class)
public class PostgreSqlGreaterQueryIT extends AbstractGreaterQueryIT
{
	@Configuration
	public static class GreaterPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
