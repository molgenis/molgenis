package org.molgenis.data;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownEntityExceptionTest extends ExceptionMessageTest {
  @Mock private EntityType entityType;
  @Mock private Attribute attribute;

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getLabel("en")).thenReturn("Identifier");
    when(entityType.getLabel("en")).thenReturn("Books");
    assertExceptionMessageEquals(new UnknownEntityException(entityType, 5), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unknown entity with 'Identifier' '5' of type 'Books'."}
    };
  }

  @Test(dataProvider = "languageMessageProviderIdOnly")
  public void testGetLocalizedMessageIdOnly(String lang, String message) {
    assertExceptionMessageEquals(new UnknownEntityException("org_example_Books", 5), lang, message);
  }

  @DataProvider(name = "languageMessageProviderIdOnly")
  public Object[][] languageMessageProviderIdOnly() {
    return new Object[][] {new Object[] {"en", "Unknown entity '5' of type 'org_example_Books'."}};
  }
}
