package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownEntityValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownEntityValueException("value", "refEntity", "attr", "attributes", 9),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unknown 'refEntity' value 'value' for entity 'attr'. (sheet: 'attributes', row 9)"
      },
      {
        "nl",
        "Onbekende 'refEntity' waarde 'value' for entiteit 'attr'. (werkblad: 'attributes', rij 9)"
      }
    };
  }
}
