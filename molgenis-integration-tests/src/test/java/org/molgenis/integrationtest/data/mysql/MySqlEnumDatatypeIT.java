package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractEnumDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlEnumDatatypeIT.EnumMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = EnumMySqlTestConfig.class)
public class MySqlEnumDatatypeIT extends AbstractEnumDatatypeIT
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
