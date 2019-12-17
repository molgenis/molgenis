package org.molgenis.app.manager.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

public class CouldNotDeleteAppExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("app-manager");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new CouldNotDeleteAppException("app"), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new CouldNotDeleteAppException("app");
    assertEquals("app", ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {new Object[] {"en", "Couldn't delete app with id app."}};
  }
}
