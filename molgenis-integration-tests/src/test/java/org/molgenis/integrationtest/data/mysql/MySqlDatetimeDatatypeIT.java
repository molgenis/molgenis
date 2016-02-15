package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDatetimeDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDatetimeDatatypeIT.DatetimeMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DatetimeMySqlTestConfig.class)
public class MySqlDatetimeDatatypeIT extends AbstractDatetimeDatatypeIT
{
	@Configuration
	public static class DatetimeMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
