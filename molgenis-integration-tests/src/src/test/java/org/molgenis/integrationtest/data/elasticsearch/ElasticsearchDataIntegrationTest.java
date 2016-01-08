package org.molgenis.integrationtest.data.elasticsearch;

import org.molgenis.integrationtest.data.DataIntegrationTest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = ElasticsearchTestConfig.class)
public class ElasticsearchDataIntegrationTest extends DataIntegrationTest
{
	@Override
	@Test
	public void testIt()
	{
		super.testIt();
	}
}
