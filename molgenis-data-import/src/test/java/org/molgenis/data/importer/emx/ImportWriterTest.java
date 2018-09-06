package org.molgenis.data.importer.emx;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.molgenis.data.DataAction;
import org.molgenis.data.EntityManager;
import org.molgenis.data.importer.*;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ImportWriterTest extends AbstractMockitoTest {
  @Mock private MetaDataService metaDataService;

  @SuppressWarnings("deprecation")
  @Mock
  private PermissionSystemService permissionSystemService;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  @Mock private EntityManager entityManager;
  @Mock private DataPersister dataPersister;

  private ImportWriter importWriter;

  @BeforeMethod
  public void setUpBeforeMethod() {
    importWriter =
        new ImportWriter(
            metaDataService,
            permissionSystemService,
            userPermissionEvaluator,
            entityManager,
            dataPersister);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testImportWriter() {
    importWriter = new ImportWriter(null, null, null, null, null);
  }

  // regression test for https://github.com/molgenis/molgenis/issues/7611
  @Test
  public void testDoImportIgnoreMetadata() {
    EmxImportJob emxImportJob = mock(EmxImportJob.class);
    when(emxImportJob.getMetadataAction()).thenReturn(MetadataAction.IGNORE);
    when(emxImportJob.getDataAction()).thenReturn(DataAction.ADD_UPDATE_EXISTING);
    ParsedMetaData parsedMetaData = mock(ParsedMetaData.class);
    when(parsedMetaData.getEntities()).thenReturn(ImmutableList.of());
    when(emxImportJob.getParsedMetaData()).thenReturn(parsedMetaData);
    EntityImportReport entityImportReport = mock(EntityImportReport.class);
    when(emxImportJob.getEntityImportReport()).thenReturn(entityImportReport);
    PersistResult persistResult = mock(PersistResult.class);
    when(dataPersister.persist(
            any(), eq(DataPersister.MetadataMode.NONE), eq(DataPersister.DataMode.UPSERT)))
        .thenReturn(persistResult);
    when(persistResult.getNrPersistedEntitiesMap()).thenReturn(ImmutableMap.of());

    importWriter.doImport(emxImportJob);
    verifyZeroInteractions(metaDataService);
  }
}
