package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractExtendsIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlExtendsIT.ExtendsPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ExtendsPostgreSqlTestConfig.class)
public class PostgreSqlExtendsIT extends AbstractExtendsIT
{
	@Configuration
	public static class ExtendsPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
