package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnsupportedAttributeTypeExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Attribute attr = mock(Attribute.class);
    when(attr.getDataType()).thenReturn(AttributeType.STRING);
    when(attr.getName()).thenReturn("myString");
    assertExceptionMessageEquals(new UnsupportedAttributeTypeException(attr), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Attribute type 'STRING' of attribute 'myString' is not suitable for retrieving subresources."
    };
    return new Object[][] {enParams};
  }
}
