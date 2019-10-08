package org.molgenis.security.permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.molgenis.data.security.EntityTypePermission.READ_DATA;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.PermissionRegistry;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {UserPermissionEvaluatorImplTest.Config.class})
@SecurityTestExecutionListeners
class UserPermissionEvaluatorImplTest extends AbstractMockitoSpringContextTests {
  @Mock private PermissionEvaluator permissionEvaluator;
  @Mock private PermissionRegistry permissionRegistry;

  private UserPermissionEvaluatorImpl userPermissionEvaluator;

  @BeforeEach
  void setUpBeforeMethod() {
    userPermissionEvaluator =
        new UserPermissionEvaluatorImpl(permissionEvaluator, permissionRegistry);
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionSetOnEntityTypeTrue() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionRegistry.getPermissions(READ_DATA)).thenReturn(ImmutableSet.of(READ));
    when(permissionEvaluator.hasPermission(authentication, "entityType0", "entityType", READ))
        .thenReturn(true);
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity("entityType0"), EntityTypePermission.READ_DATA));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionOnEntityTypeTrue() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionRegistry.getPermissions(READ_DATA))
        .thenReturn(ImmutableSet.of(READ, WRITE, WRITEMETA));
    CumulativePermission permissionToCheck =
        new CumulativePermission().set(READ).set(WRITE).set(WRITEMETA);
    when(permissionEvaluator.hasPermission(
            authentication, "entityType0", "entityType", permissionToCheck))
        .thenReturn(true);
    assertTrue(
        userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"), READ_DATA));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionSetOnEntityTypeFalse() {
    assertFalse(
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity("entityType0"), EntityTypePermission.READ_DATA));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionOnEntityTypeFalse() {
    assertFalse(
        userPermissionEvaluator.hasPermission(new EntityTypeIdentity("entityType0"), READ_DATA));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SU"})
  @Test
  void hasPermissionSetOnEntityTypeSuperuser() {
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity("entityType0"), EntityTypePermission.UPDATE_DATA));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SU"})
  @Test
  void hasPermissionOnEntityTypeSuperuser() {
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity("entityType0"), EntityTypePermission.UPDATE_METADATA));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SYSTEM"})
  @Test
  void hasPermissionSetOnEntityTypeSystemUser() {
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity("entityType0"), EntityTypePermission.UPDATE_DATA));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SYSTEM"})
  @Test
  void hasPermissionOnEntityTypeSystemUser() {
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new EntityTypeIdentity("entityType0"), EntityTypePermission.ADD_DATA));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionSetOnPluginTrue() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionRegistry.getPermissions(VIEW_PLUGIN)).thenReturn(ImmutableSet.of(READ));
    when(permissionEvaluator.hasPermission(authentication, "plugin1", "plugin", READ))
        .thenReturn(true);
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new PluginIdentity("plugin1"), PluginPermission.VIEW_PLUGIN));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionOnPluginTrue() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    when(permissionRegistry.getPermissions(VIEW_PLUGIN)).thenReturn(ImmutableSet.of(READ));
    when(permissionEvaluator.hasPermission(
            authentication, "plugin1", "plugin", new CumulativePermission().set(READ)))
        .thenReturn(true);
    assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionSetOnPluginFalse() {
    assertFalse(
        userPermissionEvaluator.hasPermission(
            new PluginIdentity("plugin1"), PluginPermission.VIEW_PLUGIN));
  }

  @WithMockUser(username = "USER")
  @Test
  void hasPermissionOnPluginFalse() {
    assertFalse(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SU"})
  @Test
  void hasPermissionSetOnPluginSuperuser() {
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new PluginIdentity("plugin1"), EntityTypePermission.READ_DATA));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SU"})
  @Test
  void hasPermissionOnPluginSuperuser() {
    assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SYSTEM"})
  @Test
  void hasPermissionSetOnPluginSystemUser() {
    assertTrue(
        userPermissionEvaluator.hasPermission(
            new PluginIdentity("plugin1"), EntityTypePermission.READ_DATA));
  }

  @WithMockUser(
      username = "USER",
      authorities = {"ROLE_SYSTEM"})
  @Test
  void hasPermissionOnPluginSystemUser() {
    assertTrue(userPermissionEvaluator.hasPermission(new PluginIdentity("plugin1"), VIEW_PLUGIN));
  }

  static class Config {}
}
