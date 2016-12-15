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
			Assert.assertTrue(dataService.hasRepository("it_emx_datatypes_Person"));
			Assert.assertTrue(dataService.hasRepository("it_emx_datatypes_TypeTestRef"));
			Assert.assertTrue(dataService.hasRepository("it_emx_datatypes_Location"));
			Assert.assertTrue(dataService.hasRepository("it_emx_datatypes_TypeTest"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("it_emx_datatypes_Person"));
			Assert.assertTrue(searchService.hasMapping("it_emx_datatypes_TypeTestRef"));
			Assert.assertTrue(searchService.hasMapping("it_emx_datatypes_Location"));
			Assert.assertTrue(searchService.hasMapping("it_emx_datatypes_TypeTest"));
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
			Assert.assertTrue(dataService.hasRepository("it_deep_advanced_p_TestEntity_2"));
			Assert.assertTrue(dataService.hasRepository("it_deep_TestCategorical_1"));
			Assert.assertTrue(dataService.hasRepository("it_deep_TestXref_1"));
			Assert.assertTrue(dataService.hasRepository("it_deep_TestXref_2"));
			Assert.assertTrue(dataService.hasRepository("it_deep_TestMref_1"));

			// Test existing in Elasticsearch
			// "base_TestEntity_0" and "advanced_TestEntity_1" are abstract;
			Assert.assertTrue(searchService.hasMapping("it_deep_advanced_p_TestEntity_2"));
			Assert.assertTrue(searchService.hasMapping("it_deep_TestCategorical_1"));
			Assert.assertTrue(searchService.hasMapping("it_deep_TestXref_1"));
			Assert.assertTrue(searchService.hasMapping("it_deep_TestXref_2"));
			Assert.assertTrue(searchService.hasMapping("it_deep_TestMref_1"));
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
			Assert.assertTrue(dataService.hasRepository("it_emx_lookupattribute_Ref1"));
			Assert.assertTrue(dataService.hasRepository("it_emx_lookupattribute_Ref2"));
			Assert.assertTrue(dataService.hasRepository("it_emx_lookupattribute_Ref3"));
			Assert.assertTrue(dataService.hasRepository("it_emx_lookupattribute_Ref4"));
			Assert.assertTrue(dataService.hasRepository("it_emx_lookupattribute_Ref5"));
			Assert.assertTrue(dataService.hasRepository("it_emx_lookupattribute_TestLookupAttributes"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("it_emx_lookupattribute_Ref1"));
			Assert.assertTrue(searchService.hasMapping("it_emx_lookupattribute_Ref2"));
			Assert.assertTrue(searchService.hasMapping("it_emx_lookupattribute_Ref3"));
			Assert.assertTrue(searchService.hasMapping("it_emx_lookupattribute_Ref4"));
			Assert.assertTrue(searchService.hasMapping("it_emx_lookupattribute_Ref5"));
			Assert.assertTrue(searchService.hasMapping("it_emx_lookupattribute_TestLookupAttributes"));
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
			Assert.assertTrue(dataService.hasRepository("it_emx_autoid_testAutoId"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("it_emx_autoid_testAutoId"));
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
			Assert.assertTrue(dataService.hasRepository("it_emx_onetomany_book"));
			Assert.assertTrue(dataService.hasRepository("it_emx_onetomany_author"));
			Assert.assertTrue(dataService.hasRepository("it_emx_onetomany_node"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("it_emx_onetomany_book"));
			Assert.assertTrue(searchService.hasMapping("it_emx_onetomany_author"));
			Assert.assertTrue(searchService.hasMapping("it_emx_onetomany_node"));
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
			Assert.assertTrue(dataService.hasRepository("it_emx_selfreferences_PersonTest"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("it_emx_selfreferences_PersonTest"));
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
			Assert.assertTrue(dataService.hasRepository("it_emx_tags_TagEntity"));

			// Test existing in Elasticsearch
			Assert.assertTrue(searchService.hasMapping("it_emx_tags_TagEntity"));
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
