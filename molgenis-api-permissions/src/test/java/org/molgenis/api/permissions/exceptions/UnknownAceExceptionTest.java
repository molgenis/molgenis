package org.molgenis.api.permissions.exceptions;

import static org.testng.Assert.assertEquals;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownAceExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownAceException("id", "type", new GrantedAuthoritySid("ROLE_role1"), "delete"),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex =
        new UnknownAceException("id", "type", new GrantedAuthoritySid("ROLE_role1"), "delete");
    assertEquals(
        ex.getMessage(),
        "typeId:id, identifier:type, sid:GrantedAuthoritySid[ROLE_role1], operation:delete");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "No permission found to delete for id 'id' of type 'type' for 'role1'."}
    };
  }
}
