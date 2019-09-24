package org.molgenis.data.importer.emx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InconsistentPackageStructureExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InconsistentPackageStructureException("pack", "parent", "packages", 2), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Package name should be fully qualified. (package: 'pack', parentpackage: 'parent' sheet: 'packages', row 2)"
      },
      {
        "nl",
        "Map naam moet beginnen met de naam van de bovenliggende map. (map: pack, bovenliggende map: parent werkblad: 'packages', rij 2)"
      }
    };
  }
}
