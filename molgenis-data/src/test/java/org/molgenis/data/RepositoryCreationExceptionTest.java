package org.molgenis.data;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class RepositoryCreationExceptionTest extends ExceptionMessageTest {
  @Mock private EntityType entityType;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    when(entityType.getLabel(lang)).thenReturn("My Entity Type");
    assertExceptionMessageEquals(new RepositoryCreationException(entityType), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Can't create repository for abstract entity type 'My Entity Type'."},
      {
        "nl",
        "Aanmaken van opslagplaats voor abstracte entiteitsoort 'My Entity Type' niet mogelijk."
      }
    };
  }
}
