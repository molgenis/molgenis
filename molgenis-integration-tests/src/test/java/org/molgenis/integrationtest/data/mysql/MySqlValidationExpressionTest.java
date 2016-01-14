package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractValidationExpressionTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlValidationExpressionTest.ValidationExpressionMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ValidationExpressionMySqlTestConfig.class)
public class MySqlValidationExpressionTest extends AbstractValidationExpressionTest
{
	@Configuration
	public static class ValidationExpressionMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
