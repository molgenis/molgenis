package org.molgenis.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class RepositoryAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new RepositoryAlreadyExistsException("MyRepository"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Repository 'MyRepository' already exists."};
    Object[] nlParams = {"nl", "Opslagplaats 'MyRepository' bestaat al."};
    return new Object[][] {enParams, nlParams};
  }
}
