package org.molgenis.core.ui.style.exception;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class GetThemeExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("core-ui");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String language, String message) {
    GetThemeExceptionTest.assertExceptionMessageEquals(
        new GetThemeException("my theme name"), language, message);
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  protected void testGetLocalizedMessageWithTrowAble(String language, String message) {
    GetThemeExceptionTest.assertExceptionMessageEquals(
        new GetThemeException("my theme name", new IOException()), language, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unable to return theme data with name: 'my theme name'."},
      new Object[] {"nl", "Niet mogelijk om thema data op te halen met de naam: 'my theme name'."}
    };
  }
}
