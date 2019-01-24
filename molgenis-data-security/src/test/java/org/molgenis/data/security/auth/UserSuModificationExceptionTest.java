package org.molgenis.data.security.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.i18n.test.exception.ExceptionMessageTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UserSuModificationExceptionTest extends ExceptionMessageTest {
  @BeforeMethod
  public void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @Test(dataProvider = "languageMessageProvider")
  @Override
  public void testGetLocalizedMessage(String lang, String message) {
    User user = when(mock(User.class).getUsername()).thenReturn("MyUsername").getMock();
    assertExceptionMessageEquals(new UserSuModificationException(user), lang, message);
  }

  @DataProvider(name = "languageMessageProvider")
  @Override
  public Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "No permission to create or modify superuser 'MyUsername'."},
      {"nl", "Geen rechten om superuser 'MyUsername' aan te maken of te wijzigen."}
    };
  }
}
