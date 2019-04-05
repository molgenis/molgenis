package org.molgenis.api.permissions.exceptions;

import static org.testng.Assert.assertEquals;

import org.mockito.Mockito;
import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.springframework.security.acls.model.ObjectIdentity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UpdatePermissionDeniedExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("api-permissions");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    ObjectIdentity objectIdentity = Mockito.mock(ObjectIdentity.class);
    Mockito.when(objectIdentity.getIdentifier()).thenReturn("identifier");
    Mockito.when(objectIdentity.getType()).thenReturn("type");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InsufficientPermissionDeniedException(objectIdentity, "superuser"), lang, message);
  }

  @Test
  public void testGetMessage() {
    ObjectIdentity objectIdentity = Mockito.mock(ObjectIdentity.class);
    Mockito.when(objectIdentity.getIdentifier()).thenReturn("identifier");
    Mockito.when(objectIdentity.getType()).thenReturn("type");
    InsufficientPermissionDeniedException ex =
        new InsufficientPermissionDeniedException(objectIdentity, "superuser");
    assertEquals(ex.getMessage(), "type:type, identifier:identifier, role:superuser");
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "User has to be superuser for this operation on row with id 'type' of type 'identifier'."
      }
    };
  }
}
