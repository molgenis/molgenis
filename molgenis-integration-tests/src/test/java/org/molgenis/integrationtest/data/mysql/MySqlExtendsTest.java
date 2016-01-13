package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractExtendsTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlExtendsTest.ExtendsMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ExtendsMySqlTestConfig.class)
public class MySqlExtendsTest extends AbstractExtendsTest
{
	@Configuration
	public static class ExtendsMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
