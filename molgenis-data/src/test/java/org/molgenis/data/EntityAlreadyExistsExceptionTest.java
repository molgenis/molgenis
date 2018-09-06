package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EntityAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @Mock private Entity entity;

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    when(entity.getIdValue()).thenReturn("id0");
    EntityType entityType =
        when(mock(EntityType.class).getLabel(lang)).thenReturn("My Entity Type").getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertExceptionMessageEquals(new EntityAlreadyExistsException(entity), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Entity 'id0' of type 'My Entity Type' already exists."},
      {"nl", "Entiteit 'id0' van entiteitsoort 'My Entity Type' bestaat al."}
    };
  }
}
