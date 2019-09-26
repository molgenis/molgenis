package org.molgenis.security.permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;

class MolgenisPermissionControllerTest {

  private UserPermissionEvaluator permissionService;
  private MolgenisPermissionController molgenisPermissionController;

  @BeforeEach
  void setUpBeforeMethod() {
    permissionService = mock(UserPermissionEvaluator.class);
    molgenisPermissionController = new MolgenisPermissionController(permissionService);
  }

  @Test
  void MolgenisPermissionController() {
    assertThrows(NullPointerException.class, () -> new MolgenisPermissionController(null));
  }

  @Test
  void hasReadPermissionTrue() {
    String entityTypeId = "entity";
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_DATA))
        .thenReturn(true);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertTrue(molgenisPermissionController.hasReadPermission(entityTypeId));
  }

  @Test
  void hasReadPermissionFalse() {
    String entityTypeId = "entity";
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_DATA))
        .thenReturn(false);
    assertFalse(molgenisPermissionController.hasReadPermission(entityTypeId));
  }

  @Test
  void hasWritePermissionTrue() {
    String entityTypeId = "entity";
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.UPDATE_DATA))
        .thenReturn(true);
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.READ_METADATA))
        .thenReturn(true);
    assertTrue(molgenisPermissionController.hasWritePermission(entityTypeId));
  }

  @Test
  void hasWritePermissionFalse() {
    String entityTypeId = "entity";
    when(permissionService.hasPermission(
            new EntityTypeIdentity(entityTypeId), EntityTypePermission.UPDATE_DATA))
        .thenReturn(false);
    assertFalse(molgenisPermissionController.hasWritePermission(entityTypeId));
  }
}
