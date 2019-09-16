package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.UnknownDataException;
import org.molgenis.util.exception.ExceptionMessageTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;

class UnknownAceExceptionTest extends ExceptionMessageTest {

  private ObjectIdentityImpl objectIdentity;

  @BeforeEach
  void setUp() {
    objectIdentity = new ObjectIdentityImpl("type", "id");
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownAceException(objectIdentity, new GrantedAuthoritySid("ROLE_role1"), "delete"),
        lang,
        message);
  }

  @Test
  void testGetMessage() {
    UnknownDataException ex =
        new UnknownAceException(objectIdentity, new GrantedAuthoritySid("ROLE_role1"), "delete");
    assertEquals(
        "typeId:type, identifier:id, sid:GrantedAuthoritySid[ROLE_role1], operation:delete",
        ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "No permission found to 'delete' for id 'id' of type 'type' for 'role1'."}
    };
  }
}
