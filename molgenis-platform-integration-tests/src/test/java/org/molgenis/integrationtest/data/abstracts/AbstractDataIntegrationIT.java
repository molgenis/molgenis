package org.molgenis.integrationtest.data.abstracts;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.reindex.job.ReindexService;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.integrationtest.data.SecuritySupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.jayway.awaitility.Awaitility;

public abstract class AbstractDataIntegrationIT extends AbstractTestNGSpringContextTests
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected DataService dataService;

	@Autowired
	protected SearchService searchService;

	@Autowired
	protected MetaDataServiceImpl metaDataService;

	@Autowired
	protected ConfigurableApplicationContext applicationContext;

	@Autowired
	protected EmbeddedElasticSearchServiceFactory embeddedElasticSearchServiceFactory;

	@Autowired
	protected ReindexService reindexService;

	@Autowired
	protected SecuritySupportService securitySupportService;

	@BeforeClass
	public void init()
	{
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
		securitySupportService.logout();

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

	/**
	 * Wait till the whole index is stable. Reindex job is done a-synchronized.
	 *
	 * @param pollInterval
	 * @param maxTimeout
	 */
	protected void waitForWholeIndexToBeStable(long pollInterval, long maxTimeout)
	{
		Awaitility.waitAtMost(maxTimeout, TimeUnit.SECONDS).pollInterval(pollInterval, TimeUnit.SECONDS)
				.until(reindexService::areAllIndiciesStable);
		logger.debug("<---- Whole index is stable ---->");
	}

	/**
	 * Wait till the index is stable. Reindex job is done a-synchronized.
	 *
	 * @param entityName
	 * @param pollInterval
	 * @param maxTimeout
	 */
	protected void waitForIndexToBeStable(String entityName, long pollInterval, long maxTimeout)
	{
		Awaitility.waitAtMost(maxTimeout, TimeUnit.SECONDS).pollInterval(pollInterval, TimeUnit.SECONDS)
				.until(() -> reindexService.isIndexStableIncludingReferences(entityName));
		logger.debug("<---- index for entity [{}] incl. references is stable ---->", entityName);
	}
}
