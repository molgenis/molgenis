package org.molgenis.data.security.permission;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityPermission.READ;
import static org.molgenis.data.security.EntityTypePermission.READ_METADATA;

import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.acl.MutableAclClassService;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class PermissionServiceDecoratorTest extends AbstractMockitoTest {

  private static SecurityContext originalSecurityContext;
  @Mock MutableAclService mutableAclService;
  @Mock MutableAclClassService mutableAclClassService;
  @Mock PermissionService permissionService;
  @Mock EntityHelper entityHelper;
  @Mock UserRoleTools userRoleTools;
  @Mock UserPermissionEvaluator userPermissionEvaluator;
  private PermissionServiceDecorator permissionServiceDecorator;

  @BeforeAll
  static void beforeClass() {
    originalSecurityContext = SecurityContextHolder.getContext();
  }

  @BeforeEach
  void setUpBeforeMethod() {
    permissionServiceDecorator =
        new PermissionServiceDecorator(
            permissionService,
            entityHelper,
            userRoleTools,
            mutableAclService,
            mutableAclClassService,
            userPermissionEvaluator);
  }

  private void setSu() {
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(
            "su", "credentials", singleton(new SimpleGrantedAuthority("ROLE_SU"))));
    SecurityContextHolder.setContext(securityContext);
  }

  private void setUser() {
    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken("user", "credentials", emptyList()));
    SecurityContextHolder.setContext(securityContext);
  }

  private static void resetContext() {
    SecurityContextHolder.setContext(originalSecurityContext);
  }

  @Test
  void testGetTypes() {
    LabelledType type1 = LabelledType.create("entity-type1", "type1", "label");
    LabelledType type2 = LabelledType.create("entity-type2", "type2", "label");
    LabelledType type3 = LabelledType.create("entity-type3", "type3", "label");
    when(permissionService.getLabelledTypes()).thenReturn(Sets.newHashSet(type1, type2, type3));
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new EntityTypeIdentity("type1"), READ);
    doReturn(false)
        .when(userPermissionEvaluator)
        .hasPermission(new EntityTypeIdentity("type2"), READ);
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new EntityTypeIdentity("type3"), READ);
    assertEquals(permissionServiceDecorator.getLabelledTypes(), Sets.newHashSet(type1, type3));
  }

  @Test
  void testGetPermissionsForObject() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissionsForObject(
        objectIdentity, Collections.singleton(sid), true);
    verify(permissionService)
        .getPermissionsForObject(objectIdentity, Collections.singleton(sid), true);
    resetContext();
  }

  @Test
  void testCreateAcl() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    permissionServiceDecorator.createAcl(objectIdentity);
    verify(permissionService).createAcl(objectIdentity);
    resetContext();
  }

  @Test
  void testCreatePermission() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);

    when(mutableAclClassService.getAclClassTypes()).thenReturn(singletonList("type"));

    permissionServiceDecorator.createPermission(permission);
    verify(permissionService).createPermission(permission);
    resetContext();
  }

  @Test
  void testCreatePermissions() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);
    when(mutableAclClassService.getAclClassTypes()).thenReturn(singletonList("type"));

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.createPermissions(Collections.singleton(permission));
    verify(permissionService).createPermissions(Collections.singleton(permission));
    resetContext();
  }

  @Test
  void testUpdatePermission() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    when(mutableAclClassService.getAclClassTypes()).thenReturn(singletonList("type"));

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.updatePermission(permission);
    verify(permissionService).updatePermission(permission);
    resetContext();
  }

  @Test
  void testUpdatePermissions() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);
    when(mutableAclClassService.getAclClassTypes()).thenReturn(singletonList("type"));
    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.updatePermissions(Collections.singleton(permission));
    verify(permissionService).updatePermissions(Collections.singleton(permission));
    resetContext();
  }

  @Test
  void testDeletePermission() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);
    when(mutableAclClassService.getAclClassTypes()).thenReturn(singletonList("type"));
    permissionServiceDecorator.deletePermission(sid, objectIdentity);
    verify(permissionService).deletePermission(sid, objectIdentity);
    resetContext();
  }

  @Test
  void testAddType() {
    setSu();
    permissionServiceDecorator.addType("entity-typeId");
    verify(permissionService).addType("entity-typeId");
    resetContext();
  }

  @Test
  void testDeleteType() {
    setSu();
    permissionServiceDecorator.deleteType("entity-typeId");
    verify(permissionService).deleteType("entity-typeId");
    resetContext();
  }

  @Test
  void testGetPermissionsForType() {
    setUser();
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissionsForType(
        "entity-typeId", Collections.singleton(sid), false);
    verify(permissionService)
        .getPermissionsForType("entity-typeId", Collections.singleton(sid), false);
    resetContext();
  }

  @Test
  void testGetPermissionsForType1() {
    setUser();
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissionsForType(
        "entity-typeId", Collections.singleton(sid), 10, 10);
    verify(permissionService)
        .getPermissionsForType("entity-typeId", Collections.singleton(sid), 10, 10);
    resetContext();
  }

  @Test
  void testGetPermissions() {
    setUser();
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissions(Collections.singleton(sid), false);
    verify(permissionService).getPermissions(Collections.singleton(sid), false);
    resetContext();
  }

  @Test
  void testGetObjects() {
    setSu();
    permissionServiceDecorator.getObjects("entity-typeId", 10, 10);
    verify(permissionService).getObjects("entity-typeId", 10, 10);
    resetContext();
  }

  @Test
  void testGetSuitablePermissionsForType() {
    setUser();
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("typeId"), READ_METADATA))
        .thenReturn(true);
    permissionServiceDecorator.getSuitablePermissionsForType("entity-typeId");
    verify(permissionService).getSuitablePermissionsForType("entity-typeId");
  }

  @Test
  void testExists() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.exists(objectIdentity, sid);
    verify(permissionService).exists(objectIdentity, sid);
    resetContext();
  }

  @AfterAll
  static void tearDown() {
    resetContext();
  }
}
