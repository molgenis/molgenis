package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractEnumDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlEnumDatatypeTest.EnumMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = EnumMySqlTestConfig.class)
public class MySqlEnumDatatypeTest extends AbstractEnumDatatypeTest
{
	@Configuration
	public static class EnumMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
