package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MissingEmxAttributeAttributeValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new MissingEmxAttributeAttributeValueException(
            "enum_options", "attr", "entityName", "attributes", 2),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Missing 'enum_options' for attribute 'attr' of entity 'entityName'. (sheet: 'attributes', row 2)"
      },
      {
        "nl",
        "Onbrekende kolom 'enum_options' voor attribuut 'attr' of entiteit 'entityName'. (werkblad: 'attributes', rij 2)"
      }
    };
  }
}
