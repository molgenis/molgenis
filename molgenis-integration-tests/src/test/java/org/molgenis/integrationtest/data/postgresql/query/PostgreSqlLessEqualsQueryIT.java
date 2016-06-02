package org.molgenis.integrationtest.data.postgresql.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractLessEqualsQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlLessEqualsQueryIT.LessEqualsPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = LessEqualsPostgreSqlTestConfig.class)
public class PostgreSqlLessEqualsQueryIT extends AbstractLessEqualsQueryIT
{
	@Configuration
	public static class LessEqualsPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
