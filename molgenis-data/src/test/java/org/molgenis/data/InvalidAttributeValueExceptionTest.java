package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.LONG;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidAttributeValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("attributeName");
    when(attribute.getDataType()).thenReturn(LONG);
    assertExceptionMessageEquals(
        new InvalidAttributeValueException(attribute, "value.type.number"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Invalid value for attribute 'attributeName' of type 'LONG', the value should be a number."
    };
    Object[] nlParams = {
      "nl",
      "Ongeldige waarde voor attribuut 'attributeName' van type 'LONG', waarde zou een nummer moeten zijn."
    };
    return new Object[][] {enParams, nlParams};
  }
}
