package org.molgenis.data.security.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

public class UserSuModificationExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    User user = when(mock(User.class).getUsername()).thenReturn("MyUsername").getMock();
    assertExceptionMessageEquals(new UserSuModificationException(user), lang, message);
  }

  public static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "No permission to create or modify superuser 'MyUsername'."},
      {"nl", "Geen rechten om superuser 'MyUsername' aan te maken of te wijzigen."}
    };
  }
}
