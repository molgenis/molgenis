package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MissingCompoundExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new MissingCompoundException(
            "partOfAttribute", "attributeName", "entityTypeId", "attributes", 3),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "partOfAttribute 'partOfAttribute' of attribute 'attributeName' of entity 'entityTypeId' must refer to an existing compound attribute. (sheet: 'attributes', row 3)"
      },
      {
        "nl",
        "partOfAttribute 'partOfAttribute' van attribuut 'attributeName' of entiteit 'entityTypeId' moet verwijzen naar een bestaand compound attribuut. (werkblad: 'attributes', rij 3)"
      }
    };
  }
}
