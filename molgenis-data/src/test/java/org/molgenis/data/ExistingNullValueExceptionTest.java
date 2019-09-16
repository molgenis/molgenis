package org.molgenis.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class ExistingNullValueExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new ExistingNullValueException("MyEntityType", "myAttributeName", mock(Throwable.class)),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    ExistingNullValueException ex =
        new ExistingNullValueException("MyEntityType", "myAttributeName", mock(Throwable.class));
    assertEquals("entityTypeId:MyEntityType attributeName:myAttributeName", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "The attribute 'myAttributeName' of entity 'MyEntityType' contains null values."
      },
      new Object[] {
        "nl", "Het attribuut 'myAttributeName' van entiteit 'MyEntityType' bevat null waarden."
      }
    };
  }
}
