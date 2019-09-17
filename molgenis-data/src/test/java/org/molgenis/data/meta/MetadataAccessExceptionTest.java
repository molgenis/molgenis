package org.molgenis.data.meta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class MetadataAccessExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new MetadataAccessException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Metadata not accessible."};
    Object[] nlParams = {"nl", "Metadata niet toegankelijk."};
    return new Object[][] {enParams, nlParams};
  }
}
