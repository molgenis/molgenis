package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractXrefDatatypeTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlXrefDatatypeTest.XrefMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = XrefMySqlTestConfig.class)
public class MySqlXrefDatatypeTest extends AbstractXrefDatatypeTest
{
	@Configuration
	public static class XrefMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt() throws Exception
	{
		super.testIt();
	}
}
