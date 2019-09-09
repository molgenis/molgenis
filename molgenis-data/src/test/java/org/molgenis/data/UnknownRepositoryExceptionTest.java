package org.molgenis.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownRepositoryExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new UnknownRepositoryException("MyRepository"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Unknown repository 'MyRepository'."};
    Object[] nlParams = {"nl", "Onbekende opslagplaats 'MyRepository'."};
    return new Object[][] {enParams, nlParams};
  }
}
