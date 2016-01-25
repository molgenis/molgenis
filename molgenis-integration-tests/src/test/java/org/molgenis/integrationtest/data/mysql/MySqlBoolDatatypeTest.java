package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractBoolDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlBoolDatatypeTest.BoolMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = BoolMySqlTestConfig.class)
public class MySqlBoolDatatypeTest extends AbstractBoolDatatypeTest
{
	@Configuration
	public static class BoolMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
