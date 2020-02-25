package org.molgenis.core.ui.data.importer.wizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.ImportRun;
import org.molgenis.data.importer.ImportRunService;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.validation.BindingResult;

@SecurityTestExecutionListeners
class ValidationResultWizardPageTest extends AbstractMockitoSpringContextTests {
  @Mock private ImportServiceFactory importServiceFactory;
  @Mock private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
  @Mock private ImportRunService importRunService;
  private ValidationResultWizardPage validationResultWizardPage;

  @BeforeEach
  void setUpBeforeEach() {
    validationResultWizardPage =
        new ValidationResultWizardPage(
            importServiceFactory, fileRepositoryCollectionFactory, importRunService);
  }

  @AfterEach
  void tearDownAfterEach() {
    validationResultWizardPage.preDestroy();
  }

  @Test
  void testGetTitle() {
    assertEquals("Validation", validationResultWizardPage.getTitle());
  }

  @Test
  @WithMockUser
  void testHandleRequest() {
    ImportRun importRun = mock(ImportRun.class);
    when(importRunService.addImportRun("user", false)).thenReturn(importRun);
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    BindingResult bindingResult = mock(BindingResult.class);
    ImportWizard wizard = mock(ImportWizard.class);
    when(wizard.getDataImportOption()).thenReturn("add");
    when(wizard.getMetadataImportOption()).thenReturn("add");
    assertNull(validationResultWizardPage.handleRequest(httpServletRequest, bindingResult, wizard));
    // this is as useful as the unit test can get:
    // job will be executed using an actual executor service and fail
  }
}
