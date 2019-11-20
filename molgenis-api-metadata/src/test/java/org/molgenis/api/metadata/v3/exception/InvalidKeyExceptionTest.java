package org.molgenis.api.metadata.v3.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidKeyExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-metadata");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new InvalidKeyException("TARGET", "KEY"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Field 'KEY' is unknown for 'TARGET' requests."};
    return new Object[][] {enParams};
  }
}
