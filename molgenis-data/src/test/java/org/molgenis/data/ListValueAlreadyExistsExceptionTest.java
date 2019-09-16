package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ListValueAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ListValueAlreadyExistsException(
            "MyEntityTypeId", "myAttributeName", "myEntityId", "myValue", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    ListValueAlreadyExistsException ex =
        new ListValueAlreadyExistsException(
            "MyEntityTypeId", "myAttributeName", "myEntityId", "myValue", mock(Throwable.class));
    assertEquals(
        "entityTypeId:MyEntityTypeId attributeName:myAttributeName entityId:myEntityId value:myValue",
        ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Duplicate list value 'myValue' for attribute 'myAttributeName' from entity 'MyEntityTypeId' with id 'myEntityId'."
      },
      new Object[] {
        "nl",
        "Gedupliceerde lijst waarde 'myValue' voor attribuut 'myAttributeName' van entities 'MyEntityTypeId' met id 'myEntityId'."
      }
    };
  }
}
