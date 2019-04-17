package org.molgenis.data.importer.emx;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_ATTRIBUTES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_I18NSTRINGS;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_LANGUAGES;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGES;
import static org.testng.Assert.*;

import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.importer.MetadataParser;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class EmxImportServiceTest extends AbstractMockitoTest {

  @Mock private DataService dataService;
  @Mock private MetadataParser metadataParser;
  @Mock private SecurityContext securityContext;
  @Mock private ImportWriter importWriter;
  private EmxImportService emxImportService;

  @BeforeMethod
  public void setUpBeforeMethod() {
    emxImportService = new EmxImportService(metadataParser, importWriter, dataService);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void testGetMetadataActionIGNORE() {
    RepositoryCollection source = mock(RepositoryCollection.class);
    doReturn(false).when(source).hasRepository(EMX_ATTRIBUTES);
    doReturn(false).when(source).hasRepository(EMX_PACKAGES);
    doReturn(false).when(source).hasRepository(EMX_LANGUAGES);
    doReturn(false).when(source).hasRepository(EMX_I18NSTRINGS);
    assertEquals(emxImportService.getMetadataAction(source), MetadataAction.IGNORE);
  }

  @Test
  public void testGetMetadataActionADD() {
    RepositoryCollection source = mock(RepositoryCollection.class);
    doReturn(true).when(source).hasRepository(EMX_ATTRIBUTES);
    assertEquals(emxImportService.getMetadataAction(source), MetadataAction.ADD);
  }

  @Test
  public void testGetMetadataActionADDPackOnly() {
    RepositoryCollection source = mock(RepositoryCollection.class);
    doReturn(false).when(source).hasRepository(EMX_ATTRIBUTES);
    doReturn(true).when(source).hasRepository(EMX_PACKAGES);
    assertEquals(emxImportService.getMetadataAction(source), MetadataAction.ADD);
  }

  @Test
  public void testGetMetadataActionADDLangOnly() {
    RepositoryCollection source = mock(RepositoryCollection.class);
    doReturn(false).when(source).hasRepository(EMX_ATTRIBUTES);
    doReturn(false).when(source).hasRepository(EMX_PACKAGES);
    doReturn(true).when(source).hasRepository(EMX_LANGUAGES);
    assertEquals(emxImportService.getMetadataAction(source), MetadataAction.ADD);
  }
}
