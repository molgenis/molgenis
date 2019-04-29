package org.molgenis.data.security.exception;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataAlreadyExistsException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DuplicatePermissionExceptionTest extends ExceptionMessageTest {

  private ObjectIdentityImpl objectIdentity;

  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
    objectIdentity = new ObjectIdentityImpl("type", "id");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new DuplicatePermissionException(objectIdentity, new GrantedAuthoritySid("ROLE_role1")),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    DataAlreadyExistsException ex =
        new DuplicatePermissionException(objectIdentity, new GrantedAuthoritySid("ROLE_role1"));
    assertEquals(
        ex.getMessage(), "typeId:type, identifier:id, sid:GrantedAuthoritySid[ROLE_role1]");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en", "'role1' already has a permission on the resource with id 'id' of type 'type'."
      }
    };
  }
}
