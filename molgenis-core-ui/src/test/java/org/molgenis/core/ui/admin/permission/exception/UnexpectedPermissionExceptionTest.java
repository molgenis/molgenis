package org.molgenis.core.ui.admin.permission.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.acls.model.Permission;

class UnexpectedPermissionExceptionTest {
  @Test
  void testUnexpectedPermissionException() {
    Permission permission = mock(Permission.class);
    when(permission.toString()).thenReturn("test");
    assertEquals(
        "Illegal permission 'test'", new UnexpectedPermissionException(permission).getMessage());
  }
}
