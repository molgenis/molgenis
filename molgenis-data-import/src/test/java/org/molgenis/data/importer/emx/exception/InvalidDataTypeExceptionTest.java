package org.molgenis.data.importer.emx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.testng.Assert.*;

import org.molgenis.data.meta.model.Attribute;
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
    Attribute attr = mock(Attribute.class);
    when(attr.getName()).thenReturn("attrname");
    when(attr.getDataType()).thenReturn(LONG);
    assertExceptionMessageEquals(
        new InvalidDataTypeException("columnType", attr, "entityType", "attributes", 7),
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
