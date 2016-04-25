package org.molgenis.integrationtest.data.postgresql.model;

import org.molgenis.integrationtest.data.abstracts.model.AbstractBoolDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.AbstractPostgreSqlTestConfig;
import org.molgenis.integrationtest.data.postgresql.model.PostgreSqlBoolDatatypeIT.BoolPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = BoolPostgreSqlTestConfig.class)
public class PostgreSqlBoolDatatypeIT extends AbstractBoolDatatypeIT
{
	@Configuration
	public static class BoolPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
