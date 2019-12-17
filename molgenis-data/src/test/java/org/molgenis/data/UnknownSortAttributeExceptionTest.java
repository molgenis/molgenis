package org.molgenis.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownSortAttributeExceptionTest extends ExceptionMessageTest {
  @Mock private EntityType entityType;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownSortAttributeException(entityType, "MyAttribute"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Unknown sort attribute 'MyAttribute'."};
    Object[] nlParams = {"nl", "Onbekend sorteer attribuut 'MyAttribute'."};
    return new Object[][] {enParams, nlParams};
  }
}
