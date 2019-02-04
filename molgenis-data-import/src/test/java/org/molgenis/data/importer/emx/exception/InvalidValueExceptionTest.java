package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidValueException("value", "attr", "true,false", "attributes", 2), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Illegal 'attr' value 'value'. Allowed values are true,false. (sheet: 'attributes', row 2)"
      },
      {
        "nl",
        "Illegale 'attr' waarde 'value'. Toegestane waardes zijn true,false. (werkblad: 'attributes', rij 2)"
      }
    };
  }
}
