package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractFileDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlFileDatatypeTest.FileMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = FileMySqlTestConfig.class)
public class MySqlFileDatatypeTest extends AbstractFileDatatypeTest
{
	@Configuration
	public static class FileMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
