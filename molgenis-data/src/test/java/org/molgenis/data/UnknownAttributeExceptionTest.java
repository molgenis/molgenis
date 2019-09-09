package org.molgenis.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.ExceptionMessageTest;

class UnknownAttributeExceptionTest extends ExceptionMessageTest {
  @Mock private EntityType entityType;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data");
    when(entityType.getLabel(any()))
        .then(
            (invocation -> {
              String language = invocation.getArgument(0);
              return language.equals("en") ? "MyEntityType" : "MijnEntiteitSoort";
            }));
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    assertExceptionMessageEquals(
        new UnknownAttributeException(entityType, "MyAttribute"), lang, message);
  }

  static Object[][] languageMessageProvider() {
    Object[] enParams = {"en", "Unknown attribute 'MyAttribute' of entity type 'MyEntityType'."};
    Object[] nlParams = {
      "nl", "Onbekend attribuut 'MyAttribute' van entiteitsoort 'MijnEntiteitSoort'."
    };
    return new Object[][] {enParams, nlParams};
  }
}
