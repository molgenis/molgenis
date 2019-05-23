package org.molgenis.data.security.exception;

import static org.testng.Assert.assertEquals;

import org.molgenis.data.UnknownDataException;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UnknownAceExceptionTest extends ExceptionMessageTest {

  private ObjectIdentityImpl objectIdentity;

  @BeforeMethod
  public void setUp() {
    objectIdentity = new ObjectIdentityImpl("type", "id");
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {

    ExceptionMessageTest.assertExceptionMessageEquals(
        new UnknownAceException(objectIdentity, new GrantedAuthoritySid("ROLE_role1"), "delete"),
        lang,
        message);
  }

  @Test
  public void testGetMessage() {
    UnknownDataException ex =
        new UnknownAceException(objectIdentity, new GrantedAuthoritySid("ROLE_role1"), "delete");
    assertEquals(
        ex.getMessage(),
        "typeId:type, identifier:id, sid:GrantedAuthoritySid[ROLE_role1], operation:delete");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "No permission found to 'delete' for id 'id' of type 'type' for 'role1'."}
    };
  }
}
