package org.molgenis.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidJobExecutionTypeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("jobs");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidJobExecutionTypeException("MyEntityType"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Invalid job execution type 'MyEntityType'."};
    Object[] nlParams = {"nl", "Ongeldig job execution type 'MyEntityType'."};
    return new Object[][] {enParams, nlParams};
  }
}
