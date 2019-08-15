package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class NullMrefValueExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("attributeName");
    assertExceptionMessageEquals(new NullMrefValueException(attribute), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Null value is not allowed for attribute 'attributeName' of type MREF, use an empty list instead."
    };
    Object[] nlParams = {
      "nl",
      "Null waarde is niet toegestaan voor attribuut 'attributeName' met type MREF, gebruik in plaats daarvan een lege lijst."
    };
    return new Object[][] {enParams, nlParams};
  }
}
