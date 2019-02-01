package org.molgenis.data.importer.emx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    when(attr.getLabel()).thenReturn("attrname");
    assertExceptionMessageEquals(
        new InvalidDataTypeException("auto", "string", attr, "entityType", "attributes", 7),
        lang,
        message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Invalid value for column 'dataType' of attribute 'null' of entity 'entityType', auto attributes can only be of data type 'string'(sheet: 'attributes', row 7)"
      },
      {
        "nl",
        "Incorrecte waarde voor kolom dataType van attribuut 'null' van entity 'entityType', auto attributen kunnen alleen van data type 'string' zijn. (werkblad: 'attributes', rij 7)"
      }
    };
  }
}
