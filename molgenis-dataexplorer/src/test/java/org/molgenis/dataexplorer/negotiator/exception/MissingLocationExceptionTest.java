package org.molgenis.dataexplorer.negotiator.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class MissingLocationExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("negotiator");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @SuppressWarnings("java:S5786")
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new MissingLocationException("http://negotiator.example.org/endpoint"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Negotiator at http://negotiator.example.org/endpoint did not return Location header in query post response."
      },
      {
        "nl",
        "Negotiator op http://negotiator.example.org/endpoint geeft geen Location header terug in de query post response."
      }
    };
  }
}
