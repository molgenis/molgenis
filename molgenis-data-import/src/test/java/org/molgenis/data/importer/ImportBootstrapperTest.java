package org.molgenis.data.importer;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.test.AbstractMockitoTest;

class ImportBootstrapperTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  private ImportBootstrapper importBootstrapper;

  @BeforeEach
  void setUpBeforeEach() {
    importBootstrapper = new ImportBootstrapper(dataService);
  }

  @Test
  void bootstrap() {
    ImportRun importRun = mock(ImportRun.class);

    @SuppressWarnings("unchecked")
    Query<ImportRun> query = mock(Query.class, RETURNS_DEEP_STUBS);
    when(query.eq("status", "RUNNING")).thenReturn(query);
    when(query.findAll()).thenReturn(Stream.of(importRun));
    when(dataService.query("sys_ImportRun", ImportRun.class)).thenReturn(query);
    importBootstrapper.bootstrap();

    verify(importRun).setStatus("FAILED");
    verify(importRun).setMessage("Application terminated unexpectedly");
    verify(dataService).update("sys_ImportRun", importRun);
  }
}
