package org.molgenis.data.meta;

import org.molgenis.util.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MetadataAccessExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new MetadataAccessException(), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Metadata not accessible."};
    Object[] nlParams = {"nl", "Metadata niet toegankelijk."};
    return new Object[][] {enParams, nlParams};
  }
}
