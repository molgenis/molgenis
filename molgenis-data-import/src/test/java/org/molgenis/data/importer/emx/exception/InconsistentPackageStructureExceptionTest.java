package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InconsistentPackageStructureExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InconsistentPackageStructureException("pack", "parent", "packages", 2), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Package name should be fully qualified. (package: pack, parentpackage: parent sheet: 'packages', row 2)"
      },
      {
        "nl",
        "Map naam moet beginnen met de naam van de bovenliggende map. (map: pack, bovenliggende map: parent werkblad: 'packages', rij 2)"
      }
    };
  }
}
