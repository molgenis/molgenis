package org.molgenis.integrationtest.data.mysql;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.integrationtest.data.CountryMetaData;
import org.molgenis.integrationtest.data.myqsl.MySqlTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(classes = MySqlTestConfig.class)
public class MySqlDataIntegrationTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Test
	public void testIt()
	{
		Repository repo = dataService.getMeta().addEntityMeta(CountryMetaData.INSTANCE);
		assertNotNull(repo);
		assertEquals(repo.getName(), CountryMetaData.INSTANCE.getName());
	}
}
