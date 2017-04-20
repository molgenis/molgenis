package org.molgenis.data.matrix.impl;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.matrix.MatrixService;
import org.molgenis.data.matrix.meta.MatrixMetadata;
import org.molgenis.data.matrix.model.Score;
import org.molgenis.file.FileStore;
import org.molgenis.util.ResourceUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MatrixServiceImplTest {

    private MatrixService matrixService;

    @BeforeTest
    private void setUp() {
        Entity entity = mock(Entity.class);

        when(entity.getString(MatrixMetadata.FILE_LOCATION)).thenReturn(ResourceUtils.getFile(getClass(), "/testmatrix.txt").getAbsolutePath());
        when(entity.getString(MatrixMetadata.SEPERATOR)).thenReturn("TAB");

        DataService dataService = mock(DataService.class);
        when(dataService.findOneById(MatrixMetadata.PACKAGE + "_" + MatrixMetadata.SIMPLE_NAME, "test")).thenReturn(entity);

        FileStore fileStore = mock(FileStore.class);

        matrixService = new MatrixServiceImpl(dataService, fileStore);
    }


    @Test
    public void getValueByIndexTest() {
        assertEquals(1.123, matrixService.getValueByIndex("test", 1, 1));
    }

    @Test
    public void getValueByNamesTest() {
        List<Score> results = matrixService.getValueByNames("test", "gene1,gene2", "hpo234,hpo123");

        assertTrue(results.contains(new Score("gene1", "hpo123", 1.123)));
        assertTrue(results.contains(new Score("gene1", "hpo234", 1.234)));
        assertTrue(results.contains(new Score("gene2", "hpo123", 2.123)));
        assertTrue(results.contains(new Score("gene2", "hpo234", 2.234)));
        assertEquals(results.size(), 4);
    }
}
