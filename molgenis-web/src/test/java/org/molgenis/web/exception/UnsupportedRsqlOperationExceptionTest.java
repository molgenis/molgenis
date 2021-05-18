package org.molgenis.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.STRING;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.CodedRuntimeException;
import org.molgenis.util.exception.ExceptionMessageTest;

@ExtendWith(MockitoExtension.class)
class UnsupportedRsqlOperationExceptionTest extends ExceptionMessageTest {
  @Mock EntityType entityType;
  @Mock Attribute attribute;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("web");
    when(entityType.getId()).thenReturn("entityTypeId");
    when(attribute.getName()).thenReturn("attributeName");
    when(attribute.getDataType()).thenReturn(STRING);
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnsupportedRsqlOperationException("=ge=", entityType, attribute), lang, message);
  }

  @Test
  void testGetMessage() {
    CodedRuntimeException ex = new UnsupportedRsqlOperationException("=ge=", entityType, attribute);
    assertEquals(
        "operator:=ge=, entityType: entityTypeId, attribute:attributeName, dataType:STRING",
        ex.getMessage());
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Can't perform RSQL query operator [=ge=] on STRING attribute 'attribute' of entity type 'entityType'."
      },
      new Object[] {
        "nl",
        "Kan RSQL query operator [=ge=] niet uitvoeren op STRING attribuut 'attribute' van entiteit 'entityType'."
      }
    };
  }
}
