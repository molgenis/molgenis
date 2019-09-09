package org.molgenis.navigator.download.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class DownloadFailedExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("navigator");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Exception cause = mock(Exception.class);
    when(cause.getLocalizedMessage()).thenReturn("panic: stuff went wrong here!");

    ExceptionMessageTest.assertExceptionMessageEquals(
        new DownloadFailedException(cause), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Download failed: panic: stuff went wrong here!"},
      {"nl", "Download gefaald: panic: stuff went wrong here!"}
    };
  }
}
