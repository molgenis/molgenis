package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ValueAlreadyExistsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ValueAlreadyExistsException(
            "MyEntityType", "myAttributeName", "myValue", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    ValueAlreadyExistsException ex =
        new ValueAlreadyExistsException(
            "MyEntityType", "myAttributeName", "myValue", mock(Throwable.class));
    assertEquals(
        "entityTypeId:MyEntityType attributeName:myAttributeName value:myValue", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Value 'myValue' for unique attribute 'myAttributeName' from entity 'MyEntityType' already exists."
      },
      new Object[] {
        "nl",
        "Waarde 'myValue' voor uniek attribuut 'myAttributeName' van entiteit 'MyEntityType' bestaat al."
      }
    };
  }
}
