package org.molgenis.data.importer.emx.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidReferenceDataTypeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-import");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Attribute attr = mock(Attribute.class);
    when(attr.getLabel()).thenReturn("attrname");
    assertExceptionMessageEquals(
        new InvalidReferenceDataTypeException("string", attr, "entityType", "attributes", 7),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Invalid value for column 'dataType' of attribute 'null' of entity 'entityType', string attributes cannot be of reference data types (sheet: 'attributes', row 7)"
      },
      {
        "nl",
        "Incorrecte waarde voor kolom dataType van attribuut 'null' van entity 'entityType', 'string' attributen kunnen niet van referentie data types zijn (werkblad: 'attributes', rij 7)"
      }
    };
  }
}
