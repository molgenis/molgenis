package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

class DuplicatePermissionExceptionTest extends ExceptionMessageTest {

  private ObjectIdentityImpl objectIdentity;

  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
    objectIdentity = new ObjectIdentityImpl("type", "id");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new DuplicatePermissionException(objectIdentity, new GrantedAuthoritySid("ROLE_role1")),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    DataAlreadyExistsException ex =
        new DuplicatePermissionException(objectIdentity, new GrantedAuthoritySid("ROLE_role1"));
    assertEquals(
        ex.getMessage(), "typeId:type, identifier:id, sid:GrantedAuthoritySid[ROLE_role1]");
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "'role1' already has a permission on the resource with id 'id' of type 'type'."
      }
    };
  }
}
