package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractAutoAttributesTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlAutoAttributesTest.AutoAttributesMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = AutoAttributesMySqlTestConfig.class)
public class MySqlAutoAttributesTest extends AbstractAutoAttributesTest
{
	@Configuration
	public static class AutoAttributesMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
