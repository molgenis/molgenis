package org.molgenis.data;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RepositoryCreationExceptionTest extends ExceptionMessageTest {
  @Mock private EntityType entityType;

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    when(entityType.getLabel(lang)).thenReturn("My Entity Type");
    assertExceptionMessageEquals(new RepositoryCreationException(entityType), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Can't create repository for abstract entity type 'My Entity Type'."},
      {
        "nl",
        "Aanmaken van opslagplaats voor abstracte entiteitsoort 'My Entity Type' niet mogelijk."
      }
    };
  }
}
