package org.molgenis.integrationtest.data.postgresql.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractOrQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlOrQueryIT.OrPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = OrPostgreSqlTestConfig.class)
public class PostgreSqlOrQueryIT extends AbstractOrQueryIT
{
	@Configuration
	public static class OrPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
