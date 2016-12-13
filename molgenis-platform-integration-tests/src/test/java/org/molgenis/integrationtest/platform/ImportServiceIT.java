package org.molgenis.integrationtest.platform;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.ImportServiceRegistrar;
import org.molgenis.data.importer.emx.EmxImportService;
import org.molgenis.data.support.FileRepositoryCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * Test the Importer test cases
 */
@TransactionConfiguration(defaultRollback = false)
@ContextConfiguration(classes = { PlatformITConfig.class })
public class ImportServiceIT extends AbstractTestNGSpringContextTests
{
	private final static Logger LOG = LoggerFactory.getLogger(ImportServiceIT.class);

	@Autowired
	EmxImportService emxImportService;

	@Autowired
	ImportServiceFactory importServiceFactory;

	@Autowired
	FileRepositoryCollectionFactory fileRepositoryCollectionFactory;

	@Autowired
	ImportServiceRegistrar importServiceRegistrar;

	@Autowired
	IndexService indexService;

	@Autowired
	DataService dataService;

	@Autowired
	SearchService searchService;

	@BeforeClass
	public void beforeClass()
	{
		ContextRefreshedEvent contextRefreshedEvent = Mockito.mock(ContextRefreshedEvent.class);
		Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
		importServiceRegistrar.register(contextRefreshedEvent);
	}

	@Test
	public void test() throws URISyntaxException, IOException
	{
		final String pathname = "/xls/it_emx_all_datatypes.xlsx";
		ClassPathResource classPath = new ClassPathResource(pathname);
		File file = classPath.getFile();
		LOG.trace("actual location: [{}]", file);

		runAsSystem(() ->
		{
			FileRepositoryCollection repoCollection = fileRepositoryCollectionFactory
					.createFileRepositoryCollection(file);
			ImportService importService = importServiceFactory.getImportService(file, repoCollection);

			// ADD
			importService.doImport(repoCollection, DatabaseAction.ADD, PACKAGE_DEFAULT);

			// ADD/UPDATE
			importService.doImport(repoCollection, DatabaseAction.ADD_UPDATE_EXISTING, PACKAGE_DEFAULT);

			// UPDATE
			importService.doImport(repoCollection, DatabaseAction.UPDATE, PACKAGE_DEFAULT);

			// Wait for index to finish
			PlatformIT.waitForWorkToBeFinished(indexService, LOG);

			// Test existing in PostgreSQL
			Assert.assertTrue(dataService.hasRepository("org_molgenis_it_Person"));
			Assert.assertTrue(dataService.hasRepository("org_molgenis_it_TypeTestRef"));
			Assert.assertTrue(dataService.hasRepository("org_molgenis_it_Location"));
			Assert.assertTrue(dataService.hasRepository("org_molgenis_it_TypeTest"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("org_molgenis_it_Person"));
			Assert.assertTrue(searchService.hasMapping("org_molgenis_it_TypeTestRef"));
			Assert.assertTrue(searchService.hasMapping("org_molgenis_it_Location"));
			Assert.assertTrue(searchService.hasMapping("org_molgenis_it_TypeTest"));
		});
	}
}
