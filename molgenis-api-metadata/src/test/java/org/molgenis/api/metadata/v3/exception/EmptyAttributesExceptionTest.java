package org.molgenis.api.metadata.v3.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class EmptyAttributesExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-metadata");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new EmptyAttributesException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "An entityType cannot have zero attributes."};
    return new Object[][] {enParams};
  }
}
