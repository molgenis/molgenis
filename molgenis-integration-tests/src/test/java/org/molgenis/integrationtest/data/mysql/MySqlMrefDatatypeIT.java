package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractMrefDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlMrefDatatypeIT.MrefMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MrefMySqlTestConfig.class)
public class MySqlMrefDatatypeIT extends AbstractMrefDatatypeTest
{
	@Configuration
	public static class MrefMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
