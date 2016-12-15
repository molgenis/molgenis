package org.molgenis.integrationtest.platform;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataAccessException;
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

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
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
	public void testEmxDatatypes()
	{
		final String pathname = "/xls/it_emx_datatypes.xlsx";
		File file = getFile(pathname);

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
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "Person")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "TypeTestRef")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "Location")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "TypeTest")));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "Person")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "TypeTestRef")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "Location")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "datatypes", "TypeTest")));
		});
	}

	@Test
	public void testEmxDeepNesting()
	{
		final String pathname = "/xls/it_emx_deep_nesting.xlsx";
		File file = getFile(pathname);

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
			// "base_TestEntity_0" and "advanced_TestEntity_1" are abstract;

			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "deep", "advanced", "p", "TestEntity_2")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestCategorical_1")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestXref_1")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestXref_2")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestMref_1")));

			// Test existing in Elasticsearch
			// "base_TestEntity_0" and "advanced_TestEntity_1" are abstract;

			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "deep", "advanced", "p", "TestEntity_2")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestCategorical_1")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestXref_1")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestXref_2")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "deep", "TestMref_1")));
		});
	}

	@Test
	public void testEmxItLookupAttribute()
	{
		final String pathname = "/xls/it_emx_lookup_attribute.xlsx";
		File file = getFile(pathname);

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
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref1")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref2")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref3")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref4")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref5")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "TestLookupAttributes")));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref1")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref2")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref3")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref4")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "Ref5")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "lookupattribute", "TestLookupAttributes")));
		});
	}

	@Test
	public void testEmxAutoId()
	{
		final String pathname = "/xls/it_emx_autoid.xlsx";
		File file = getFile(pathname);

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
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "autoid", "testAutoId")));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "autoid", "testAutoId")));
		});
	}

	@Test
	public void testEmxOneToMany()
	{
		final String pathname = "/xls/it_emx_onetomany.xlsx";
		File file = getFile(pathname);

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
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "onetomany", "book")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "onetomany", "author")));
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "onetomany", "node")));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "onetomany", "book")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "onetomany", "author")));
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "onetomany", "node")));
		});
	}

	@Test
	public void testEmxSelfReferences()
	{
		final String pathname = "/xls/it_emx_self_references.xlsx";
		File file = getFile(pathname);

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
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "selfreferences", "PersonTest")));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "selfreferences", "PersonTest")));
		});
	}

	@Test
	public void testEmxTags()
	{
		final String pathname = "/xls/it_emx_tags.xlsx";
		File file = getFile(pathname);

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
			Assert.assertTrue(dataService.hasRepository(String.join(PACKAGE_SEPARATOR, "it", "emx", "tags", "TagEntity")));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping(String.join(PACKAGE_SEPARATOR, "it", "emx", "tags", "TagEntity")));
		});
	}

	private File getFile(String pathname)
	{
		requireNonNull(pathname);
		ClassPathResource classPath = new ClassPathResource(pathname);

		try
		{
			File file = classPath.getFile();
			LOG.trace("emx import integration test file: [{}]", file);
			return file;
		}
		catch (Exception e)
		{
			LOG.error("File name: [{}]", pathname);
			throw new MolgenisDataAccessException(e);
		}
	}
}
