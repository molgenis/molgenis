package org.molgenis.api.permissions.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class PageWithoutPageSizeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new PageWithoutPageSizeException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {new Object[] {"en", "Please provide both page and pageSize."}};
  }
}
