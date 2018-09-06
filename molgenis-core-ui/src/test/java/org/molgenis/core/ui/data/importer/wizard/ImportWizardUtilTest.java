package org.molgenis.core.ui.data.importer.wizard;

import static org.mockito.Mockito.*;

import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.testng.annotations.Test;

public class ImportWizardUtilTest {

  @Test
  public void testHandleException() {
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
