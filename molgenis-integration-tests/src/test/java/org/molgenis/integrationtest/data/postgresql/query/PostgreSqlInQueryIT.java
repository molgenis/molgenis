package org.molgenis.integrationtest.data.postgresql.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractInQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlInQueryIT.InPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = InPostgreSqlTestConfig.class)
public class PostgreSqlInQueryIT extends AbstractInQueryIT
{
	@Configuration
	public static class InPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
