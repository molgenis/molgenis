package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractCompoundDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlCompoundDatatypeTest.CompoundMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = CompoundMySqlTestConfig.class)
public class MySqlCompoundDatatypeTest extends AbstractCompoundDatatypeTest
{
	@Configuration
	public static class CompoundMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
