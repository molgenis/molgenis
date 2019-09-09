package org.molgenis.api.data.v3;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;

class NullMrefValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Attribute attribute = mock(Attribute.class);
    when(attribute.getName()).thenReturn("attributeName");
    assertExceptionMessageEquals(new NullMrefValueException(attribute), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en",
      "Null value is not allowed for attribute 'attributeName' of type MREF, use an empty list instead."
    };
    return new Object[][] {enParams};
  }
}
