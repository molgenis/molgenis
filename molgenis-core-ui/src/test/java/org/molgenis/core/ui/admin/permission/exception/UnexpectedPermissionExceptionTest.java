package org.molgenis.core.ui.admin.permission.exception;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.springframework.security.acls.model.Permission;
import org.testng.annotations.Test;

public class UnexpectedPermissionExceptionTest {
  @Test
  public void testUnexpectedPermissionException() {
    Permission permission = mock(Permission.class);
    when(permission.toString()).thenReturn("test");
    assertEquals(
        new UnexpectedPermissionException(permission).getMessage(), "Illegal permission 'test'");
  }
}
