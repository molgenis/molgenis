package org.molgenis.integrationtest.data.postgresql;

import org.molgenis.integrationtest.data.AbstractValidationExpressionIT;
import org.molgenis.integrationtest.data.postgresql.PostgreSqlValidationExpressionIT.ValidationExpressionPostgreSqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ValidationExpressionPostgreSqlTestConfig.class)
public class PostgreSqlValidationExpressionIT extends AbstractValidationExpressionIT
{
	@Configuration
	public static class ValidationExpressionPostgreSqlTestConfig extends AbstractPostgreSqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
