package org.molgenis.integrationtest.data.postgresql.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractNotQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlNotQueryIT.NotPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = NotPostgreSqlTestConfig.class)
public class PostgreSqlNotQueryIT extends AbstractNotQueryIT
{
	@Configuration
	public static class NotPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
