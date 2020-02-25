package org.molgenis.data.security.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.security.auth.Role;
import org.molgenis.util.exception.ExceptionMessageTest;

class CircularRoleHierarchyExceptionTest extends ExceptionMessageTest {
  @BeforeEach
  void setUp() {
    messageSource.addMolgenisNamespaces("data-security");
  }

  @ParameterizedTest
  @MethodSource("languageMessageProvider")
  @Override
  protected void testGetLocalizedMessage(String lang, String message) {
    Role role = mock(Role.class);
    when(role.getId()).thenReturn("test");
    when(role.getName()).thenReturn("test_VIEWER");
    ExceptionMessageTest.assertExceptionMessageEquals(
        new CircularRoleHierarchyException(role), lang, message);
  }

  @Test
  void testGetMessage() {
    Role role = mock(Role.class);
    when(role.getId()).thenReturn("test");
    when(role.getName()).thenReturn("test_VIEWER");
    CircularRoleHierarchyException ex = new CircularRoleHierarchyException(role);
    assertEquals("id:test,name:test_VIEWER", ex.getMessage());
  }

  static Object[][] languageMessageProvider() {
    return new Object[][] {
      new Object[] {"en", "Role with name 'test_VIEWER' has a circular role hierarchy."}
    };
  }
}
