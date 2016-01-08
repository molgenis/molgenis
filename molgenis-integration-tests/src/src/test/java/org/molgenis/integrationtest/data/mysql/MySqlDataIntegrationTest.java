package org.molgenis.integrationtest.data.mysql;

import org.molgenis.integrationtest.data.DataIntegrationTest;
import org.molgenis.integrationtest.data.myqsl.MySqlTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MySqlTestConfig.class)
public class MySqlDataIntegrationTest extends DataIntegrationTest
{
	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
