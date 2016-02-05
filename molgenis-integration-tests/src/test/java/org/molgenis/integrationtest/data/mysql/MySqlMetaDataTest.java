package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.AbstractMetaDataTest;
import org.molgenis.integrationtest.data.myqsl.AbstractMySqlTestConfig;
import org.molgenis.integrationtest.data.mysql.MySqlMetaDataTest.MetaDataMySqlTestConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MetaDataMySqlTestConfig.class)
public class MySqlMetaDataTest extends AbstractMetaDataTest
{
	@Configuration
	public static class MetaDataMySqlTestConfig extends AbstractMySqlTestConfig
	{
	}

	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
