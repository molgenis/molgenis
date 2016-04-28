package org.molgenis.integrationtest.data.postgresql.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractAndQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlAndQueryIT.AndPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = AndPostgreSqlTestConfig.class)
public class PostgreSqlAndQueryIT extends AbstractAndQueryIT
{
	@Configuration
	public static class AndPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
