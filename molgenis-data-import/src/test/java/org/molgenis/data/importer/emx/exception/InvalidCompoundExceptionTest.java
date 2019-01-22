package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidCompoundExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidCompoundException("attr", "attrName", "entityTypeId", "attributes", 3),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "partOfAttribute 'attr' of attribute attrName of entity entityTypeId must refer to a attribute of type 'compound'. (sheet: 'attributes', row 3)"
      },
      {
        "nl",
        "partOfattribuut 'attr' van attribuut 'attrName' van entiteit 'entityTypeId' moet refereren naar een attribuut van type compound. (werkblad: 'attributes', rij 3)"
      }
    };
  }
}
