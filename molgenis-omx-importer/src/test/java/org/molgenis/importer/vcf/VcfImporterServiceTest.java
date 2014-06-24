package org.molgenis.importer.vcf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.elasticsearch.config.ElasticSearchClient;
import org.springframework.util.FileCopyUtils;
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
        FileRepositoryCollection fileRepositoryCollection = mock(FileRepositoryCollection.class);
        InputStream in_data = VcfImporterServiceTest.class.getResourceAsStream("/testdata.vcf");
        File testdata = new File(FileUtils.getTempDirectory(), "testdata.vcf");
        FileCopyUtils.copy(in_data, new FileOutputStream(testdata));
        when(fileRepositoryCollectionFactory.createFileRepositoryCollection(testdata)).thenReturn(fileRepositoryCollection);
        when(fileRepositoryCollection.getEntityNames()).thenReturn(Collections.singletonList(entityName));
        vcfImporterService.importVcf(testdata, "test");
	}
}
