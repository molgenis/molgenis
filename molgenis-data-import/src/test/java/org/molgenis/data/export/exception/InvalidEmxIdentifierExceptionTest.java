package org.molgenis.data.export.exception;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidEmxIdentifierExceptionTest extends ExceptionMessageTest {

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Entity entity = mock(Entity.class);
    EntityType entityType = mock(EntityType.class);
    when(entity.getEntityType()).thenReturn(entityType);
    when(entity.getLabelValue()).thenReturn("label");
    doReturn("entityTypeLabel").when(entityType).getLabel(lang);
    doReturn("entityTypeId").when(entityType).getLabel();
    assertExceptionMessageEquals(new InvalidEmxIdentifierException(entity), lang, message);
  }

  static Object[][] languageMessageProvider() {
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
