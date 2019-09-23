package org.molgenis.data.excel.xlsx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class MaximumSheetNameLengthExceededExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("excel");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new MaximumSheetNameLengthExceededException("Thisisasheennamethatexceedsthelimit"),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "The entity type name 'Thisisasheennamethatexceedsthelimit' is too long to be used as a sheet name in XLSX."
      },
      {
        "nl",
        "De entiteitsoort naam  'Thisisasheennamethatexceedsthelimit' is te lang om als tabblad naam in XLSX gebruikt te worden."
      }
    };
  }
}
