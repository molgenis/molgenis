package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DuplicateAttributeNameExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new DuplicateAttributeNameException("attr", "entity", "attributes", 20), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Duplicate attribute name 'attr' for entity type 'entity'.(sheet: 'attributes', row 20)"
      },
      {
        "nl",
        "Dubbele attribuut name 'attr' for entiteit type 'entity'. (werkblad: 'attributes', rij 20)"
      }
    };
  }
}
