package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractDateDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlDateDatatypeIT.DateMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DateMySqlTestConfig.class)
public class MySqlDateDatatypeIT extends AbstractDateDatatypeIT
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
