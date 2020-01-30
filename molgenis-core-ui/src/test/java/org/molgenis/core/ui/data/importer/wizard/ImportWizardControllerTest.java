package org.molgenis.core.ui.data.importer.wizard;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.DataAction;
import org.molgenis.data.DataService;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.importer.ImportJob;
import org.molgenis.data.importer.ImportRun;
import org.molgenis.data.importer.ImportRunService;
import org.molgenis.data.importer.ImportService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.data.importer.MetadataAction;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@ContextConfiguration(classes = {ImportWizardControllerTest.Config.class})
@SecurityTestExecutionListeners
class ImportWizardControllerTest extends AbstractMockitoSpringContextTests {
  private static final String USERNAME = "user";

  @Mock private UploadWizardPage uploadWizardPage;
  @Mock private OptionsWizardPage optionsWizardPage;
  @Mock private PackageWizardPage packageWizardPage;
  @Mock private ValidationResultWizardPage validationResultWizardPage;
  @Mock private ImportResultsWizardPage importResultsWizardPage;
  @Mock private DataService dataService;
  @Mock private ImportServiceFactory importServiceFactory;
  @Mock private FileStore fileStore;
  @Mock private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
  @Mock private ImportRunService importRunService;
  @Mock private ExecutorService executorService;

  private ImportWizardController importWizardController;

  @BeforeEach
  void setUpBeforeMethod() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    importWizardController =
        new ImportWizardController(
            uploadWizardPage,
            optionsWizardPage,
            packageWizardPage,
            validationResultWizardPage,
            importResultsWizardPage,
            dataService,
            importServiceFactory,
            fileStore,
            fileRepositoryCollectionFactory,
            importRunService);
    importWizardController.setExecutorService(executorService);
  }

  static Iterator<Object[]> testInitProvider() {
    return asList(
            new Object[] {"add", DataAction.ADD, "upsert", MetadataAction.UPSERT},
            new Object[] {"update", DataAction.UPDATE, "ignore", MetadataAction.IGNORE})
        .iterator();
  }

  @ParameterizedTest
  @MethodSource("testInitProvider")
  @WithMockUser(username = USERNAME)
  void testImportFile(
      String dataActionStr,
      DataAction dataAction,
      String metadataActionStr,
      MetadataAction metadataAction)
      throws IOException, URISyntaxException {
    HttpSession httpSession = mock(HttpSession.class);
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    when(httpServletRequest.getSession()).thenReturn(httpSession);

    String filename = "example.xlsx";
    MultipartFile multipartFile = createMultipartFile(filename);
    String entityTypeId = null;
    String packageId = null;
    Boolean notify = null;

    String tmpFileName = "tmp-example.xlsx";
    File tmpFile = mock(File.class);
    when(tmpFile.getName()).thenReturn(tmpFileName);
    when(fileStore.store(any(), eq("example.xlsx"))).thenReturn(tmpFile);

    ImportService importService = mock(ImportService.class);
    when(importServiceFactory.getImportService(tmpFileName)).thenReturn(importService);

    String importRunEntityTypeId = "entityTypeId";
    EntityType entityType =
        when(mock(EntityType.class).getId()).thenReturn(importRunEntityTypeId).getMock();

    String importRunIdValue = "importRunId";
    ImportRun importRun = mock(ImportRun.class);
    when(importRun.getId()).thenReturn(importRunIdValue);
    when(importRun.getIdValue()).thenReturn(importRunIdValue);
    when(importRun.getEntityType()).thenReturn(entityType);
    when(importRunService.addImportRun(USERNAME, false)).thenReturn(importRun);

    @SuppressWarnings("ConstantConditions")
    ResponseEntity<String> responseEntity =
        importWizardController.importFile(
            httpServletRequest,
            multipartFile,
            entityTypeId,
            packageId,
            metadataActionStr,
            dataActionStr,
            notify);
    assertEquals(
        created(new URI("/api/v2/entityTypeId/importRunId"))
            .contentType(TEXT_PLAIN)
            .body("/api/v2/entityTypeId/importRunId"),
        responseEntity);

    verify(fileStore).store(any(), eq(filename));
    ArgumentCaptor<ImportJob> importJobArgumentCaptor = ArgumentCaptor.forClass(ImportJob.class);
    verify(executorService).execute(importJobArgumentCaptor.capture());
    assertEquals(
        new ImportJob(
            importService,
            getContext(),
            null,
            metadataAction,
            dataAction,
            importRunIdValue,
            importRunService,
            httpSession,
            null,
            Thread.currentThread().getId()),
        importJobArgumentCaptor.getValue());
  }

  @Test
  void testImportFileDataActionUnknown() throws IOException, URISyntaxException {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

    String filename = "example.xlsx";
    MultipartFile multipartFile = createMultipartFile(filename);
    String entityTypeId = null;
    String packageId = null;
    String metadataAction = "add";
    String action = "unknownAction";
    Boolean notify = null;

    String tmpFileName = "tmp-example.xlsx";
    File tmpFile = mock(File.class);
    when(tmpFile.getName()).thenReturn(tmpFileName);
    when(fileStore.store(any(), eq("example.xlsx"))).thenReturn(tmpFile);

    @SuppressWarnings("ConstantConditions")
    ResponseEntity<String> responseEntity =
        importWizardController.importFile(
            httpServletRequest,
            multipartFile,
            entityTypeId,
            packageId,
            metadataAction,
            action,
            notify);
    assertEquals(
        badRequest()
            .contentType(TEXT_PLAIN)
            .body(
                "Invalid action:[UNKNOWNACTION] valid values: [ADD, ADD_UPDATE_EXISTING, UPDATE, ADD_IGNORE_EXISTING]"),
        responseEntity);

    verify(fileStore).store(any(), eq(filename));
    verifyZeroInteractions(executorService);
  }

  @Test
  void testImportFileMetadataActionUnknown() throws IOException, URISyntaxException {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

    String filename = "example.xlsx";
    MultipartFile multipartFile = createMultipartFile(filename);
    String entityTypeId = null;
    String packageId = null;
    String metadataAction = "unknownAction";
    String action = "add";
    Boolean notify = null;

    String tmpFileName = "tmp-example.xlsx";
    File tmpFile = mock(File.class);
    when(tmpFile.getName()).thenReturn(tmpFileName);
    when(fileStore.store(any(), eq("example.xlsx"))).thenReturn(tmpFile);

    @SuppressWarnings("ConstantConditions")
    ResponseEntity<String> responseEntity =
        importWizardController.importFile(
            httpServletRequest,
            multipartFile,
            entityTypeId,
            packageId,
            metadataAction,
            action,
            notify);
    assertEquals(
        badRequest()
            .contentType(TEXT_PLAIN)
            .body("Invalid action:[UNKNOWNACTION] valid values: [ADD, UPDATE, UPSERT, IGNORE]"),
        responseEntity);

    verify(fileStore).store(any(), eq(filename));
    verifyZeroInteractions(executorService);
  }

  private MultipartFile createMultipartFile(String filename) throws IOException {
    File file = new File("/src/test/resources/" + filename);

    DiskFileItem fileItem =
        new DiskFileItem(
            "file", "text/plain", false, file.getName(), (int) file.length(), file.getParentFile());
    fileItem.getOutputStream();
    return new CommonsMultipartFile(fileItem);
  }

  static class Config {}
}
