package org.molgenis.data.validation;

import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

@MockitoSettings(strictness = LENIENT)
class AttributeExpressionParseExceptionTest extends ExceptionMessageTest {
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
    final var exception =
        new AttributeExpressionParseException(
            "{foo", attribute, "Expected \"}\":1:5, found \"\"", 4);
    assertExceptionMessageEquals(exception, lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Failed to parse expression '{foo' for attribute 'Date of birth'. Parse error message was: 'Expected \"}\":1:5, found \"\"'."
      },
      new Object[] {
        "nl",
        "Kan expressie '{foo' voor attribuut 'Geboortedatum' niet parsen. De foutmelding was: 'Expected \"}\":1:5, found \"\"'."
      }
    };
  }
}
