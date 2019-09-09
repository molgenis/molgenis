package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownEnumValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownEnumValueException("MyEntityType", "myAttributeName", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    UnknownEnumValueException ex =
        new UnknownEnumValueException("MyEntityType", "myAttributeName", mock(Throwable.class));
    assertEquals(ex.getMessage(), "entityTypeId:MyEntityType attributeName:myAttributeName");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "Unknown enum value for attribute 'myAttributeName' of entity 'MyEntityType'."
      },
      new Object[] {
        "nl", "Onbekende enum waarde voor attribuut 'myAttributeName' van entiteit 'MyEntityType'."
      }
    };
  }
}
