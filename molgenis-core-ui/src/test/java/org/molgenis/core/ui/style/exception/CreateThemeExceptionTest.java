package org.molgenis.core.ui.style.exception;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class CreateThemeExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("core-ui");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String language, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new CreateThemeException("my filename", mock(Throwable.class)), language, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unable to save style file with name: 'my filename'."},
      new Object[] {"nl", "Niet mogelijk om stijl bestand op te slaan met de naam: 'my filename'."}
    };
  }
}
