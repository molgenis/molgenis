package org.molgenis.data.importer.vcf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.meta.ElasticsearchAttributeMetaDataRepository;
import org.molgenis.data.elasticsearch.meta.ElasticsearchEntityMetaDataRepository;
import org.molgenis.data.meta.AttributeMetaDataRepository;
import org.molgenis.data.meta.EntityMetaDataRepository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.springframework.util.FileCopyUtils;
import org.testng.annotations.Test;
import org.molgenis.data.importer.vcf.VcfImporterService;

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
        DataService dataService = Mockito.mock(DataService.class);
        SearchService searchService = Mockito.mock(SearchService.class);
        Mockito.when(dataService.hasRepository(entityName)).thenReturn(true);
        FileRepositoryCollectionFactory fileRepositoryCollectionFactory = Mockito.mock(FileRepositoryCollectionFactory.class);
        VcfImporterService vcfImporterService = new VcfImporterService(fileRepositoryCollectionFactory, dataService,
                searchService);
        FileRepositoryCollection fileRepositoryCollection = Mockito.mock(FileRepositoryCollection.class);
        InputStream in_data = VcfImporterServiceTest.class.getResourceAsStream("/testdata.vcf");
        File testdata = new File(FileUtils.getTempDirectory(), "testdata.vcf");
        FileCopyUtils.copy(in_data, new FileOutputStream(testdata));
        Mockito.when(fileRepositoryCollectionFactory.createFileRepositoryCollection(testdata)).thenReturn(fileRepositoryCollection);
        Mockito.when(fileRepositoryCollection.getEntityNames()).thenReturn(Collections.singletonList(entityName));
        vcfImporterService.importVcf(testdata);
    }
}
