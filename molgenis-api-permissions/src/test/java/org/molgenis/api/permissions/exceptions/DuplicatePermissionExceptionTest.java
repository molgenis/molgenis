package org.molgenis.api.permissions.exceptions;

import static org.testng.Assert.assertEquals;

import org.molgenis.i18n.CodedRuntimeException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DuplicatePermissionExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new DuplicatePermissionException("type", "id", new GrantedAuthoritySid("ROLE_role1")),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    CodedRuntimeException ex =
        new DuplicatePermissionException("type", "id", new GrantedAuthoritySid("ROLE_role1"));
    assertEquals(
        ex.getMessage(), "typeId:type, identifier:id, sid:GrantedAuthoritySid[ROLE_role1]");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "'role1' already has a permission on the resource with id 'id' of type 'type'."
      },
      {"nl", "'role1' heeft al een permissie op de bron met id 'id' van type 'type'."}
    };
  }
}
