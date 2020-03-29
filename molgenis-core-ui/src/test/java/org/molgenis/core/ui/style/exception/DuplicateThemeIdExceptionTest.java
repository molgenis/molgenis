package org.molgenis.core.ui.style.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class DuplicateThemeIdExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("core-ui");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String language, String message) {
    DuplicateThemeIdExceptionTest.assertExceptionMessageEquals(
        new DuplicateThemeIdException("my theme id"), language, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "A style with the same identifier 'my theme id' already exists."},
      new Object[] {"nl", "Er bestaat al een slijl met dezelfde indetificatie code: 'my theme id'."}
    };
  }
}
