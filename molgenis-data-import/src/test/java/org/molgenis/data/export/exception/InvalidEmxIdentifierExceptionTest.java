package org.molgenis.data.export.exception;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
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
  public void testGetLocalizedMessageNL(String lang, String message) {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getLabelValue()).thenReturn("label");
    doReturn("entityTypeLabel").when(entityType).getLabel(lang);
    doReturn("entityTypeId").when(entityType).getLabel();
    assertExceptionMessageEquals(new InvalidEmxIdentifierException(entity), lang, message);
  }

  @Override
  public void testGetLocalizedMessage(String language, String message) {}

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "nl",
        "entityTypeLabel 'label' kan niet worden gedownload, de identifier start niet met de (bovenliggende)mapnaam."
      },
      new Object[] {
        "en",
        "entityTypeLabel 'label' cannot be downloaded, the identifier does not start with the (parent)package name."
      }
    };
  }
}
