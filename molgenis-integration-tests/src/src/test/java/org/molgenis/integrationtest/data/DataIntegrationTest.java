package org.molgenis.integrationtest.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.transaction.AsyncTransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;

public abstract class DataIntegrationTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private MetaDataServiceImpl metaDataService;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	private AsyncTransactionLog asyncTransactionLog;

	public void testIt()
	{
		Repository repo = dataService.getMeta().addEntityMeta(CountryMetaData.INSTANCE);
		assertNotNull(repo);
		assertEquals(repo.getName(), CountryMetaData.ENTITY_NAME);

		Entity nl = new DefaultEntity(CountryMetaData.INSTANCE, dataService);
		nl.set(CountryMetaData.CODE, "nl");
		nl.set(CountryMetaData.NAME, "Nederland");

		try
		{
			dataService.add(CountryMetaData.INSTANCE.getName(), nl);
			fail("Should have thrown MolgenisDataAccessException.");
		}
		catch (MolgenisDataAccessException e)
		{
			// Expected
		}

		SecuritySupport.login();
		dataService.add(CountryMetaData.ENTITY_NAME, nl);

		Entity retrieved = dataService.findOne(CountryMetaData.ENTITY_NAME, nl.getIdValue());
		assertNotNull(retrieved);
	}

	@AfterClass
	public void cleanUp()
	{
		asyncTransactionLog.stop();

		try
		{
			// Give asyncTransactionLog time to stop gracefully
			TimeUnit.SECONDS.sleep(1);
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}

		applicationContext.close();
		SecuritySupport.logout();

		try
		{
			// Stop ES
			embeddedElasticSearchServiceFactory.close();

			// Delete molgenis home folder
			FileUtils.deleteDirectory(new File(System.getProperty("molgenis.home")));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}
}
