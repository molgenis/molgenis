package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class EntityAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @Mock private Entity entity;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  protected void testGetLocalizedMessage(String lang, String message) {
    when(entity.getIdValue()).thenReturn("id0");
    EntityType entityType =
        when(mock(EntityType.class).getLabel(lang)).thenReturn("My Entity Type").getMock();
    when(entity.getEntityType()).thenReturn(entityType);
    assertExceptionMessageEquals(new EntityAlreadyExistsException(entity), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Entity 'id0' of type 'My Entity Type' already exists."},
      {"nl", "Entiteit 'id0' van entiteitsoort 'My Entity Type' bestaat al."}
    };
  }
}
