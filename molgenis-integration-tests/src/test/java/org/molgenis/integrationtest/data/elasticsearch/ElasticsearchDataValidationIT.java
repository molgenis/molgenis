package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.AbstractDataValidationIT;
import org.molgenis.integrationtest.data.elasticsearch.ElasticsearchDataValidationIT.DataValidationElasticsearchTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = DataValidationElasticsearchTestConfig.class)
public class ElasticsearchDataValidationIT extends AbstractDataValidationIT
{
	@Configuration
	public static class DataValidationElasticsearchTestConfig extends AbstractElasticsearchTestConfig
	{
	}

	@Override
	@Test
	public void testInt()
	{
		super.testInt();
	}

	@Override
	@Test
	public void testBool()
	{
		super.testBool();
	}

	@Override
	@Test
	public void testDate()
	{
		super.testDate();
	}

	@Override
	@Test
	public void testDateTime()
	{
		super.testDateTime();
	}

	@Override
	@Test
	public void testDecimal()
	{
		super.testDecimal();
	}

	@Override
	@Test
	public void testEnum()
	{
		super.testEnum();
	}

	@Override
	@Test
	public void testXref()
	{
		super.testXref();
	}

	@Override
	@Test
	public void testMref()
	{
		super.testMref();
	}

	@Override
	@Test
	public void testRange()
	{
		super.testRange();
	}

	@Override
	@Test
	public void testNotNillable()
	{
		super.testNotNillable();
	}
}
