package org.molgenis.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownEntityTypeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new UnknownEntityTypeException("MyEntityType"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Unknown entity type 'MyEntityType'."};
    Object[] nlParams = {"nl", "Onbekende entiteitsoort 'MyEntityType'."};
    return new Object[][] {enParams, nlParams};
  }
}
