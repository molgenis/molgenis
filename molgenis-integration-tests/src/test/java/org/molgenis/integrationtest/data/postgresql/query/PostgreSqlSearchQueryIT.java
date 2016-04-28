package org.molgenis.integrationtest.data.postgresql.query;

import org.molgenis.integrationtest.data.abstracts.query.AbstractSearchQueryIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.query.PostgreSqlSearchQueryIT.SearchPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.text.ParseException;

@ContextConfiguration(classes = SearchPostgreSqlTestConfig.class)
public class PostgreSqlSearchQueryIT extends AbstractSearchQueryIT
{
	@Configuration
	public static class SearchPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws ParseException
	{
		super.testIt();
	}
}
