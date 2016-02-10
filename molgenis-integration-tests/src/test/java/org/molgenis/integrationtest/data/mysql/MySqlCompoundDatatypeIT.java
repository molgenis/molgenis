package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractCompoundDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlCompoundDatatypeIT.CompoundMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = CompoundMySqlTestConfig.class)
public class MySqlCompoundDatatypeIT extends AbstractCompoundDatatypeIT
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
