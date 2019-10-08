package org.molgenis.data.importer.emx;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataAction;
import org.molgenis.data.EntityManager;
import org.molgenis.data.importer.DataPersister;
import org.molgenis.data.importer.EntityImportReport;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.importer.ParsedMetaData;
import org.molgenis.data.importer.PersistResult;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class ImportWriterTest extends AbstractMockitoTest {
  @Mock private MetaDataService metaDataService;

  @SuppressWarnings("deprecation")
  @Mock
  private PermissionSystemService permissionSystemService;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  @Mock private EntityManager entityManager;
  @Mock private DataPersister dataPersister;

  private ImportWriter importWriter;

  @BeforeEach
  void setUpBeforeMethod() {
    importWriter =
        new ImportWriter(
            metaDataService,
            permissionSystemService,
            userPermissionEvaluator,
            entityManager,
            dataPersister);
  }

  @Test
  void testImportWriter() {
    assertThrows(NullPointerException.class, () -> new ImportWriter(null, null, null, null, null));
  }

  // regression test for https://github.com/molgenis/molgenis/issues/7611
  @Test
  void testDoImportIgnoreMetadata() {
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
