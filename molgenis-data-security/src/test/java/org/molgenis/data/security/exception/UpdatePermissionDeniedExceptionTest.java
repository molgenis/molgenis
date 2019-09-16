package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.springframework.security.acls.model.ObjectIdentity;

class UpdatePermissionDeniedExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ObjectIdentity objectIdentity = Mockito.mock(ObjectIdentity.class);
    Mockito.when(objectIdentity.getIdentifier()).thenReturn("identifier");
    Mockito.when(objectIdentity.getType()).thenReturn("type");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InsufficientPermissionsException(
            objectIdentity, Collections.singletonList("superuser")),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    ObjectIdentity objectIdentity = Mockito.mock(ObjectIdentity.class);
    Mockito.when(objectIdentity.getIdentifier()).thenReturn("identifier");
    Mockito.when(objectIdentity.getType()).thenReturn("type");
    InsufficientPermissionsException ex =
        new InsufficientPermissionsException(
            objectIdentity, Collections.singletonList("superuser"));
    assertEquals("type:type, identifier:identifier, roles:[superuser]", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "User has to be one of the following: 'superuser' for this operation on row with id 'identifier' of type 'type'."
      }
    };
  }
}
