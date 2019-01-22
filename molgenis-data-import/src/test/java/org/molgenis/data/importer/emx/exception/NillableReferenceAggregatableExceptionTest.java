package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class NillableReferenceAggregatableExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new NillableReferenceAggregatableException(
            "entity", "attr", AttributeType.CATEGORICAL, "attributes", 3),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Nullable attribute 'attr' of entity 'entity' cannot be of type 'CATEGORICAL'. (sheet: 'attributes', row 3)"
      },
      {
        "nl",
        "Optioneel attribuut 'attr' van entiteit 'entity' kan niet van type 'CATEGORICAL' zijn. (werkblad: 'attributes', rij 3)"
      }
    };
  }
}
