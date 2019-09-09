package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ReadonlyValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ReadonlyValueException(
            "MyEntityType", "myAttributeName", "myEntityId", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    ReadonlyValueException ex =
        new ReadonlyValueException(
            "MyEntityType", "myAttributeName", "myEntityId", mock(Throwable.class));
    assertEquals(
        ex.getMessage(),
        "entityTypeId:MyEntityType attributeName:myAttributeName entityId:myEntityId");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Updating read-only attribute 'myAttributeName' of type 'MyEntityType' with id 'myEntityId' is not allowed."
      },
      new Object[] {
        "nl",
        "Updaten van alleen-lezen attribuut 'myAttributeName' van type 'MyEntityType' met id 'myEntityId' is niet toegestaan."
      }
    };
  }
}
