package org.molgenis.data.excel.xlsx.exception;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnsupportedValueExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("excel");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    List<String> list = Arrays.asList("test1", "test2");

    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnsupportedValueException(list), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Class 'ArrayList' of value '[test1, test2]' is not of a supported type for the XLXS writer."
      },
      {
        "nl",
        "Klasse 'ArrayList' van waarde '[test1, test2]' is niet van een ondersteund type voor de XLXS writer."
      }
    };
  }
}
