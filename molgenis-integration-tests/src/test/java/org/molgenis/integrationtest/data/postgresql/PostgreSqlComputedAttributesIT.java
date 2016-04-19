package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractComputedAttributesIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlComputedAttributesIT.ComputedAttributesPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ComputedAttributesPostgreSqlTestConfig.class)
public class PostgreSqlComputedAttributesIT extends AbstractComputedAttributesIT
{
	@Configuration
	public static class ComputedAttributesPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
