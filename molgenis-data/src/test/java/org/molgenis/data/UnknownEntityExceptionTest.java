package org.molgenis.data;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownEntityExceptionTest extends ExceptionMessageTest {
  @Mock private EntityType entityType;
  @Mock private Attribute attribute;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    when(entityType.getIdAttribute()).thenReturn(attribute);
    when(attribute.getLabel("en")).thenReturn("Identifier");
    when(entityType.getLabel("en")).thenReturn("Books");
    assertExceptionMessageEquals(new UnknownEntityException(entityType, 5), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Unknown entity with 'Identifier' '5' of type 'Books'."}
    };
  }

  @ParameterizedTest
  @MethodSource("languageMessageProviderIdOnly")
  void testGetLocalizedMessageIdOnly(String lang, String message) {
    assertExceptionMessageEquals(new UnknownEntityException("org_example_Books", 5), lang, message);
  }

  static Object[][] languageMessageProviderIdOnly() {
    return new Object[][] {new Object[] {"en", "Unknown entity '5' of type 'org_example_Books'."}};
  }
}
