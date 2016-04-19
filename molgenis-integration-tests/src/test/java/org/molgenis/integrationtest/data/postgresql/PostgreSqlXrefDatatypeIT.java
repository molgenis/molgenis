package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractXrefDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlXrefDatatypeIT.XrefPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = XrefPostgreSqlTestConfig.class)
public class PostgreSqlXrefDatatypeIT extends AbstractXrefDatatypeIT
{
	@Configuration
	public static class XrefPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
