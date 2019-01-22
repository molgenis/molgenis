package org.molgenis.data.importer.emx.exception;

import static org.testng.Assert.*;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidDataTypeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new InvalidDataTypeException(
            "columnType", AttributeType.LONG, "attrname", "entityType", "attributes", 7),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Invalid value for attribute 'attrname' van entity 'entityType', columnType attributes can only be of data type 'LONG'(sheet: 'attributes', row 7)"
      },
      {
        "nl",
        "Incorrecte waarde voor attribute 'attrname' van entity 'entityType', columnType attributen kunnen alleen van data type 'LONG' zijn. (werkblad: 'attributes', rij 7)"
      }
    };
  }
}
