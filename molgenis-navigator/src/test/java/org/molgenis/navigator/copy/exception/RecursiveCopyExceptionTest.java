package org.molgenis.navigator.copy.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class RecursiveCopyExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("navigator");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new RecursiveCopyException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "You can't copy a package into itself."};
    Object[] nlParams = {"nl", "Je kunt een map niet naar zichzelf kopiëren."};
    return new Object[][] {enParams, nlParams};
  }
}
