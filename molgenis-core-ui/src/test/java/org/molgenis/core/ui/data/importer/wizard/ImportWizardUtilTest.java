package org.molgenis.core.ui.data.importer.wizard;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

class ImportWizardUtilTest {

  @Test
  void testHandleException() {
    Exception exception =
        when(mock(Exception.class).getLocalizedMessage()).thenReturn("message").getMock();
    ImportWizard importWizard = mock(ImportWizard.class);
    BindingResult bindingResult = mock(BindingResult.class);
    Logger logger = mock(Logger.class);
    String dataImportOption = "dataImportOption";
    ImportWizardUtil.handleException(
        exception, importWizard, bindingResult, logger, dataImportOption);
    verify(bindingResult)
        .addError(new ObjectError("wizard", "<b>Your import failed:</b><br />message"));
  }
}
