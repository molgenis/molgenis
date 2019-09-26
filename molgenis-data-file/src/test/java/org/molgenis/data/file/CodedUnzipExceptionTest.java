package org.molgenis.data.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class CodedUnzipExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("file");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new CodedUnzipException("name"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Error unzipping file 'name'."};
    return new Object[][] {enParams};
  }
}
