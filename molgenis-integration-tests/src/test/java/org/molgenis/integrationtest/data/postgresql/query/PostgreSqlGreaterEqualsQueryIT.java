package org.molgenis.integrationtest.data.postgresql.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractGreaterEqualsQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlGreaterEqualsQueryIT.GreaterEqualsPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = GreaterEqualsPostgreSqlTestConfig.class)
public class PostgreSqlGreaterEqualsQueryIT extends AbstractGreaterEqualsQueryIT
{
	@Configuration
	public static class GreaterEqualsPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
