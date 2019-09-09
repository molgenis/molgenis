package org.molgenis.data.security.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.util.exception.ExceptionMessageTest;

class InsufficientInheritancePermissionsExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    ExceptionMessageTest.assertExceptionMessageEquals(
        new InsufficientInheritancePermissionsException(), lang, message);
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {
        "en",
        "Read permissions on 'sys_sec_Role' and 'sys_sec_RoleMembership' are required to be able to show inherited permissions."
      }
    };
  }
}
