package org.molgenis.navigator.copy.exception;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownCopyExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("navigator");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownCopyFailedException(mock(Throwable.class)), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "An error occurred during copying."};
    Object[] nlParams = {"nl", "Er is een fout opgetreden tijdens het kopiëren."};
    return new Object[][] {enParams, nlParams};
  }
}
