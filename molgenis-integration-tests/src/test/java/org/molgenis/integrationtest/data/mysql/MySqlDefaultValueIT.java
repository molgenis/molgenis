package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDefaultValueIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDefaultValueIT.DefaultValueMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DefaultValueMySqlTestConfig.class)
public class MySqlDefaultValueIT extends AbstractDefaultValueIT
{
	@Configuration
	public static class DefaultValueMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
