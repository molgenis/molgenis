package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class MissingRootPackageExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new MissingRootPackageException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Missing root package. There must be at least one package without a parent. (sheet: 'packages')"
      },
      {
        "nl",
        "Onbrekende hoofdmap. Minstens 1 map zonder bovenliggende map nodig. (werkblad: 'packages')"
      }
    };
  }
}
