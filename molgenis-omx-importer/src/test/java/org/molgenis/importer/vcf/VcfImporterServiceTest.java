package org.molgenis.importer.vcf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.elasticsearch.config.ElasticSearchClient;
import org.testng.annotations.Test;

public class VcfImporterServiceTest
{
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void VcfImporterService()
	{
		new VcfImporterService(null, null, null);
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void importVcf_repositoryExists() throws IOException
	{
		String entityName = "entity";
		DataService dataService = mock(DataService.class);
		when(dataService.hasRepository(entityName)).thenReturn(true);
		ElasticSearchClient client = mock(ElasticSearchClient.class);
		FileRepositoryCollectionFactory fileRepositoryCollectionFactory = mock(FileRepositoryCollectionFactory.class);
		VcfImporterService vcfImporterService = new VcfImporterService(fileRepositoryCollectionFactory, dataService,
				client);
		File vcfFile = mock(File.class);
		vcfImporterService.importVcf(vcfFile, "test");
	}
}
