package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractComputedAttributesTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlComputedAttributesTest.ComputedAttributesMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ComputedAttributesMySqlTestConfig.class)
public class MySqlComputedAttributesTest extends AbstractComputedAttributesTest
{
	@Configuration
	public static class ComputedAttributesMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
