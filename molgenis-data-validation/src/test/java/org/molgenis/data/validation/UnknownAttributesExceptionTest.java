package org.molgenis.data.validation;

import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import java.util.LinkedHashSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

@MockitoSettings(strictness = LENIENT)
class UnknownAttributesExceptionTest extends ExceptionMessageTest {
  @Mock Attribute attribute;
  @Mock EntityType entity;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-validation");
    when(attribute.getLabel("en")).thenReturn("Date of birth");
    when(attribute.getLabel("nl")).thenReturn("Geboortedatum");
    when(attribute.getEntity()).thenReturn(entity);
    when(entity.getId()).thenReturn("Person");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    var attributes = new LinkedHashSet();
    attributes.add("foo");
    attributes.add("bar");
    final var exception = new UnknownAttributesException(attribute, attributes);
    assertExceptionMessageEquals(exception, lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unknown attributes [foo, bar] mentioned in expression for attribute 'Date of birth'."
      },
      new Object[] {
        "nl",
        "Onbekende attributen '[foo, bar]' gevonden in expressie voor attribuut 'Geboortedatum'."
      }
    };
  }
}
