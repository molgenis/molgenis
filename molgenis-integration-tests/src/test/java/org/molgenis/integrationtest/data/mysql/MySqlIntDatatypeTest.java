package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractIntDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlIntDatatypeTest.IntMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = IntMySqlTestConfig.class)
public class MySqlIntDatatypeTest extends AbstractIntDatatypeTest
{
	@Configuration
	public static class IntMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
