package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractStringDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlStringDatatypeIT.StringMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = StringMySqlTestConfig.class)
public class MySqlStringDatatypeIT extends AbstractStringDatatypeIT
{
	@Configuration
	public static class StringMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
