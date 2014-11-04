package org.molgenis.data.vcf.importer;

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
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.factory.EmbeddedElasticSearchServiceFactory;
import org.molgenis.data.elasticsearch.index.EntityToSourceConverter;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.vcf.VcfRepositoryCollection;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.Test;

public class VcfImporterServiceTest
{
	@Test
	public void testImport() throws IOException
	{
		DataService dataService = new DataServiceImpl();

		EmbeddedElasticSearchServiceFactory factory = new EmbeddedElasticSearchServiceFactory("vcf-import-test");
		try
		{
			SearchService searchService = factory.create(dataService, new EntityToSourceConverter());

			File f = ResourceUtils.getFile(getClass(), "/testdata.vcf");
			VcfRepositoryCollection source = new VcfRepositoryCollection(f);

			VcfImporterService importer = new VcfImporterService(new FileRepositoryCollectionFactory(), dataService,
					searchService);

			EntityImportReport report = importer.doImport(source, DatabaseAction.ADD);

			assertNotNull(report);
			assertEquals(report.getNewEntities(), Arrays.asList("testdata", "testdata_Sample"));

			Map<String, Integer> importedEntitiesMap = report.getNrImportedEntitiesMap();
			assertNotNull(importedEntitiesMap);
			assertTrue(importedEntitiesMap.containsKey("testdata"));
			assertEquals(importedEntitiesMap.get("testdata"), Integer.valueOf(7));
			assertTrue(importedEntitiesMap.containsKey("testdata_Sample"));
			assertEquals(importedEntitiesMap.get("testdata_Sample"), Integer.valueOf(7));
		}
		finally
		{
			factory.close();
		}

	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void VcfImporterService()
	{
		new VcfImporterService(null, null, null);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void importVcf_repositoryExists() throws IOException
	{
		String entityName = "entity";
		DataService dataService = Mockito.mock(DataService.class);

		SearchService searchService = Mockito.mock(SearchService.class);
		Mockito.when(dataService.hasRepository(entityName)).thenReturn(true);

		FileRepositoryCollectionFactory fileRepositoryCollectionFactory = Mockito
				.mock(FileRepositoryCollectionFactory.class);
		VcfImporterService vcfImporterService = new VcfImporterService(fileRepositoryCollectionFactory, dataService,
				searchService);
		FileRepositoryCollection fileRepositoryCollection = Mockito.mock(FileRepositoryCollection.class);

		File testdata = new File(FileUtils.getTempDirectory(), "testdata.vcf");

		Mockito.when(fileRepositoryCollectionFactory.createFileRepositoryCollection(testdata)).thenReturn(
				fileRepositoryCollection);
		Mockito.when(fileRepositoryCollection.getEntityNames()).thenReturn(Collections.singletonList(entityName));

		Repository repo = Mockito.mock(Repository.class);
		Mockito.when(repo.getName()).thenReturn(entityName);
		Mockito.when(fileRepositoryCollection.getRepositoryByEntityName(entityName)).thenReturn(repo);

		vcfImporterService.importVcf(testdata);
	}

}
