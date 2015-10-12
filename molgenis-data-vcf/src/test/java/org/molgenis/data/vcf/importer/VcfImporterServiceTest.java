package org.molgenis.data.vcf.importer;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.DatabaseAction;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Package;
import org.molgenis.data.Repository;
import org.molgenis.data.mem.InMemoryRepositoryCollection;
import org.molgenis.data.meta.MetaDataServiceImpl;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.validation.EntityAttributesValidator;
import org.molgenis.data.validation.RepositoryValidationDecorator;
import org.molgenis.data.vcf.VcfRepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

public class VcfImporterServiceTest
{
	@Test
	public void testImport() throws IOException
	{
		DataServiceImpl dataService = new DataServiceImpl();
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataService);
		metaDataService.setDefaultBackend(new InMemoryRepositoryCollection("ElasticSearch"));
		dataService.setMeta(metaDataService);

		File f = ResourceUtils.getFile(getClass(), "/testdata.vcf");
		VcfRepositoryCollection source = new VcfRepositoryCollection(f);

		VcfImporterService importer = new VcfImporterService(new FileRepositoryCollectionFactory(), dataService,
				mock(PermissionSystemService.class));

		EntityImportReport report = importer.doImport(source, DatabaseAction.ADD, Package.DEFAULT_PACKAGE_NAME);

		assertNotNull(report);
		assertEquals(report.getNewEntities(), Arrays.asList("testdata_Sample", "testdata"));

		Map<String, Integer> importedEntitiesMap = report.getNrImportedEntitiesMap();
		assertNotNull(importedEntitiesMap);
		assertTrue(importedEntitiesMap.containsKey("testdata"));
		assertEquals(importedEntitiesMap.get("testdata"), Integer.valueOf(7));
		assertTrue(importedEntitiesMap.containsKey("testdata_Sample"));
		assertEquals(importedEntitiesMap.get("testdata_Sample"), Integer.valueOf(7));

	}

	// Regression test for https://github.com/molgenis/molgenis/issues/3351
	@Test
	public void testImportLargeInsertDelete() throws IOException
	{
		DataServiceImpl dataService = new DataServiceImpl();
		MetaDataServiceImpl metaDataService = new MetaDataServiceImpl(dataService);
		metaDataService.setDefaultBackend(new InMemoryRepositoryCollection("ElasticSearch")
		{
			// enable data validation
			@Override
			public Repository addEntityMeta(EntityMetaData entityMetaData)
			{
				Repository repo = super.addEntityMeta(entityMetaData);
				return new RepositoryValidationDecorator(dataService, repo, new EntityAttributesValidator());
			}
		});
		dataService.setMeta(metaDataService);

		File f = ResourceUtils.getFile(getClass(), "/testlargeinsertdelete.vcf");
		VcfRepositoryCollection source = new VcfRepositoryCollection(f);

		VcfImporterService importer = new VcfImporterService(new FileRepositoryCollectionFactory(), dataService,
				mock(PermissionSystemService.class));

		EntityImportReport report = importer.doImport(source, DatabaseAction.ADD, Package.DEFAULT_PACKAGE_NAME);

		assertNotNull(report); // test if no exceptions occur
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void importVcf_repositoryExists() throws IOException
	{
		String entityName = "entity";
		DataService dataService = Mockito.mock(DataService.class);
		Mockito.when(dataService.hasRepository(entityName)).thenReturn(true);

		FileRepositoryCollectionFactory fileRepositoryCollectionFactory = Mockito
				.mock(FileRepositoryCollectionFactory.class);
		VcfImporterService vcfImporterService = new VcfImporterService(fileRepositoryCollectionFactory, dataService,
				mock(PermissionSystemService.class));

		FileRepositoryCollection fileRepositoryCollection = Mockito.mock(FileRepositoryCollection.class);

		File testdata = new File(FileUtils.getTempDirectory(), "testdata.vcf");

		Mockito.when(fileRepositoryCollectionFactory.createFileRepositoryCollection(testdata))
				.thenReturn(fileRepositoryCollection);
		Mockito.when(fileRepositoryCollection.getEntityNames()).thenReturn(Collections.singletonList(entityName));

		Repository repo = Mockito.mock(Repository.class);
		Mockito.when(repo.getName()).thenReturn(entityName);
		Mockito.when(fileRepositoryCollection.getRepository(entityName)).thenReturn(repo);

		vcfImporterService.importVcf(testdata);
	}
}
