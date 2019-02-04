package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SelfReferencingCompoundExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new SelfReferencingCompoundException(
            "partOfAttribute", "attr", "entityTypeId", "attributes", 4),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "partOfAttribute 'partOfAttribute' of attribute 'attr' of entity 'entityTypeId' cannot refer to itself. (sheet: 'attributes', row 4)"
      },
      {
        "nl",
        "partOfAttribute 'partOfAttribute' van attribuut 'attr' of entiteit 'entityTypeId' kan niet naar zichzelf verwijzen. (werkblad: 'attributes', rij 4)"
      }
    };
  }
}
