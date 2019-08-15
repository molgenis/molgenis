package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.LONG;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidAttributeValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("attributeName");
    when(attribute.getDataType()).thenReturn(LONG);
    assertExceptionMessageEquals(
        new InvalidAttributeValueException(attribute, "number"), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Invalid value for attribute 'attributeName' of type 'LONG', the value should be a number."
    };
    Object[] nlParams = {
      "nl",
      "Ongeldige waarde voor attribuut 'attributeName' van type 'LONG', waarde zou een nummer moeten zijn."
    };
    return new Object[][] {enParams, nlParams};
  }
}
