package org.molgenis.api.metadata.v3.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownLookupAttributesExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("api-metadata");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    EntityType entityType = mock(EntityType.class);
    when(entityType.getId()).thenReturn("entityType");
    assertExceptionMessageEquals(
        new UnknownLookupAttributesException(entityType, Arrays.asList("test1,test2")),
        lang,
        message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {
      "en", "Unknown lookup attribute(s) 'test1,test2' for EntityType 'entityType'."
    };
    return new Object[][] {enParams};
  }
}
