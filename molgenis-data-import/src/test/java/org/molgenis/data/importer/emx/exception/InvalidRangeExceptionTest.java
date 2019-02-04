package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidRangeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidRangeException("one", "entityId", "rangeMax", "attributeName", "entities", 8),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Invalid value 'one' for column 'rangeMax' for attribute 'attributeName' of entity 'entityId', should be a long(sheet: 'entities', row 8)"
      },
      {
        "nl",
        "Incorrecte waarde 'one' voor kolom 'rangeMax' voor attribuut 'attributeName' van entiteit 'entityId', waarde moet van type long zijn. (werkblad: 'entities', rij 8)"
      }
    };
  }
}
