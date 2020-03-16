package org.molgenis.core.ui.data.importer.wizard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.importer.ImportServiceFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

class UploadWizardPageTest extends AbstractMockitoTest {
  @Mock private ImportServiceFactory importServiceFactory;
  @Mock private FileRepositoryCollectionFactory fileRepositoryCollectionFactory;
  private UploadWizardPage uploadWizardPage;

  @BeforeEach
  void setUpBeforeEach() {
    uploadWizardPage = new UploadWizardPage(importServiceFactory, fileRepositoryCollectionFactory);
  }

  @Test
  void testGetTitle() {
    assertEquals("Upload file", uploadWizardPage.getTitle());
  }

  @Test
  void testHandleRequestNoFileSelected() {
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    BindingResult bindingResult = mock(BindingResult.class);
    ImportWizard wizard = mock(ImportWizard.class);
    assertNull(uploadWizardPage.handleRequest(httpServletRequest, bindingResult, wizard));
    verify(bindingResult).addError(new ObjectError("wizard", "No file selected"));
  }
}
