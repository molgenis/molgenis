package org.molgenis.api.convert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.api.model.Query.Operator;
import org.molgenis.util.exception.ExceptionMessageTest;

class MissingRsqlValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new MissingRsqlValueException(Operator.GREATER_THAN), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "The value cannot be empty for query operator 'GREATER_THAN'."}
    };
  }
}
