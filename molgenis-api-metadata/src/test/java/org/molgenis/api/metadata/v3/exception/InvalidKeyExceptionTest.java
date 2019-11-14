package org.molgenis.api.metadata.v3.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.ExceptionMessageTest;

class InvalidKeyExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-metadata");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(new InvalidKeyException("TARGET","KEY"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
        "en",
        "Field 'TARGET' is unknown for 'KEY' requests."
    };
    return new Object[][] {enParams};
  }
}
