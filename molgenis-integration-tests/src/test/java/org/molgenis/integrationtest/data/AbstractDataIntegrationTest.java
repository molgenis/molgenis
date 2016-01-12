package org.molgenis.integrationtest.data;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.transaction.AsyncTransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;

public abstract class AbstractDataIntegrationTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	DataService dataService;

	@Autowired
	MetaDataServiceImpl metaDataService;

	@Autowired
	ConfigurableApplicationContext applicationContext;

	@Autowired
	EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	AsyncTransactionLog asyncTransactionLog;

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
