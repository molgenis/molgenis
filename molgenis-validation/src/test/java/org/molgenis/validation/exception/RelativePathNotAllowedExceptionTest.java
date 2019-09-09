package org.molgenis.validation.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

public class RelativePathNotAllowedExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("validation");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new RelativePathNotAllowedException(), lang, message);
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Relative paths are not allowed here, please specify an absolute URL."}
    };
  }
}
