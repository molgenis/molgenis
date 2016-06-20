package org.molgenis.integrationtest.data.elasticsearch.model;

import org.molgenis.integrationtest.data.AbstractValidationExpressionIT;
import org.molgenis.integrationtest.data.elasticsearch.AbstractElasticsearchTestConfig;
import org.molgenis.integrationtest.data.elasticsearch.model.ElasticsearchValidationExpressionIT.ValidationExpressionElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ValidationExpressionElasticsearchTestConfig.class)
public class ElasticsearchValidationExpressionIT extends AbstractValidationExpressionIT
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
