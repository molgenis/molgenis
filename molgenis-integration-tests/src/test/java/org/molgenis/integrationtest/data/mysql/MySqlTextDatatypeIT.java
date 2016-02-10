package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractTextDatatypeIT;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlTextDatatypeIT.TextMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = TextMySqlTestConfig.class)
public class MySqlTextDatatypeIT extends AbstractTextDatatypeIT
{
	@Configuration
	public static class TextMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
