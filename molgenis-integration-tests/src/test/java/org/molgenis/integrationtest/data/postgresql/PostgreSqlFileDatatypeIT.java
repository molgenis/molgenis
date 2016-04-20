package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractFileDatatypeIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlFileDatatypeIT.FilePostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = FilePostgreSqlTestConfig.class)
public class PostgreSqlFileDatatypeIT extends AbstractFileDatatypeIT
{
	@Configuration
	public static class FilePostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
