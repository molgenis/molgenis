package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDateDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDateDatatypeTest.DateMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DateMySqlTestConfig.class)
public class MySqlDateDatatypeTest extends AbstractDateDatatypeTest
{
	@Configuration
	public static class DateMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
