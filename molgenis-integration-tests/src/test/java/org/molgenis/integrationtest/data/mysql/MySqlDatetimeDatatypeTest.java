package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDatetimeDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDatetimeDatatypeTest.DatetimeMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DatetimeMySqlTestConfig.class)
public class MySqlDatetimeDatatypeTest extends AbstractDatetimeDatatypeTest
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
