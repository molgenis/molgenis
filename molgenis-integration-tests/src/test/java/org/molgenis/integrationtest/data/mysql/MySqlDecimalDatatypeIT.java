package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDecimalDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDecimalDatatypeIT.DecimalMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DecimalMySqlTestConfig.class)
public class MySqlDecimalDatatypeIT extends AbstractDecimalDatatypeIT
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
