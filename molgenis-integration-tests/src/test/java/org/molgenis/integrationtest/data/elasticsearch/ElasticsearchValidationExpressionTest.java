package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractValidationExpressionTest;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchValidationExpressionTest.ValidationExpressionElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ValidationExpressionElasticsearchTestConfig.class)
public class ElasticsearchValidationExpressionTest extends AbstractValidationExpressionTest
{
	@Configuration
	public static class ValidationExpressionElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
