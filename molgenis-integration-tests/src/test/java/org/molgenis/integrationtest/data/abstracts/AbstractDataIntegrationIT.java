package org.molgenis.integrationtest.data.abstracts;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.integrationtest.data.SecuritySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AbstractDataIntegrationIT extends AbstractTestNGSpringContextTests
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected DataService dataService;

	@Autowired
	protected MetaDataServiceImpl metaDataService;

	@Autowired
	protected ConfigurableApplicationContext applicationContext;

	@Autowired
	protected EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@BeforeClass
	public void init()
	{
		SecuritySupport.login();
	}

	@AfterClass
	public void cleanUp()
	{
		try
		{
			// Give asyncTransactionLog time to stop gracefully
			TimeUnit.SECONDS.sleep(1);
		}
		catch (InterruptedException e)
		{
			logger.error("InterruptedException sleeping 1 second", e);
		}

		applicationContext.close();
		SecuritySupport.logout();

		try
		{
			// Delete molgenis home folder
			FileUtils.deleteDirectory(new File(System.getProperty("molgenis.home")));
		}
		catch (IOException e)
		{
			logger.error("Error removing molgenis home directory", e);
		}

	}
}
