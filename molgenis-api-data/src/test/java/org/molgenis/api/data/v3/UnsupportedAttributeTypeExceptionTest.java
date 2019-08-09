package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnsupportedAttributeTypeExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(attr.getName()).thenReturn("myString");
    assertExceptionMessageEquals(new UnsupportedAttributeTypeException(attr), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Attribute type 'STRING' of attribute 'myString' is not suitable for retrieving subresources."
    };
    return new Object[][] {enParams};
  }
}
