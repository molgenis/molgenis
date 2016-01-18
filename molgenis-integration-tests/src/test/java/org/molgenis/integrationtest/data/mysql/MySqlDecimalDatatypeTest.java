package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDecimalDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDecimalDatatypeTest.DecimalMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DecimalMySqlTestConfig.class)
public class MySqlDecimalDatatypeTest extends AbstractDecimalDatatypeTest
{
	@Configuration
	public static class DecimalMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
