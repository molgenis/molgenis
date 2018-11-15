package org.molgenis.data.export.exception;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InvalidEmxIdentifierExceptionTest extends ExceptionMessageTest {

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new InvalidEmxIdentifierException("autoid"), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Entity type 'autoid' cannot be downloaded, the identifier does not start with the package name."
      },
      {
        "nl",
        "Entiteitsoort 'autoid' kan niet worden gedownload, de identifier start niet met de mapnaam."
      }
    };
  }
}
