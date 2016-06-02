package org.molgenis.integrationtest.data.postgresql.query;

import java.text.ParseException;

import org.molgenis.integrationtest.data.abstracts.query.AbstractRangeQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlRangeQueryIT.RangePostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = RangePostgreSqlTestConfig.class)
public class PostgreSqlRangeQueryIT extends AbstractRangeQueryIT
{
	@Configuration
	public static class RangePostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
