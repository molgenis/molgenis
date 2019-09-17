package org.molgenis.data;

import static org.molgenis.data.RepositoryCapability.WRITABLE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class RepositoryNotCapableExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new RepositoryNotCapableException("MyRepository", WRITABLE), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Repository 'MyRepository' does not support 'writing'."};
    Object[] nlParams = {"nl", "Opslagplaats 'MyRepository' ondersteunt geen 'schrijven'."};
    return new Object[][] {enParams, nlParams};
  }
}
