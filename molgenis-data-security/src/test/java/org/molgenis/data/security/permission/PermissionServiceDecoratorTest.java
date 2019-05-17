package org.molgenis.data.security.permission;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.EntityPermission.READ;
import static org.molgenis.data.security.EntityTypePermission.READ_METADATA;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.Sets;
import java.util.Collections;
import org.mockito.Mock;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.permission.model.LabelledType;
import org.molgenis.data.security.permission.model.Permission;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionServiceDecoratorTest extends AbstractMockitoTest {

  private SecurityContext originalSecurityContext;
  @Mock MutableAclService mutableAclService;
  @Mock PermissionService permissionService;
  @Mock EntityHelper entityHelper;
  @Mock UserRoleTools userRoleTools;
  @Mock UserPermissionEvaluator userPermissionEvaluator;
  private PermissionServiceDecorator permissionServiceDecorator;

  @BeforeClass
  public void beforeClass() {
    originalSecurityContext = SecurityContextHolder.getContext();
  }

  @BeforeMethod
  public void setUpBeforeMethod() {
    permissionServiceDecorator =
        new PermissionServiceDecorator(
            permissionService,
            entityHelper,
            userRoleTools,
            mutableAclService,
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

  private void resetContext() {
    SecurityContextHolder.setContext(originalSecurityContext);
  }

  @Test
  public void testGetTypes() {
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
  public void testGetPermissionsForObject() {
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
  public void testCreateAcl() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    permissionServiceDecorator.createAcl(objectIdentity);
    verify(permissionService).createAcl(objectIdentity);
    resetContext();
  }

  @Test
  public void testCreatePermission() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.createPermission(permission);
    verify(permissionService).createPermission(permission);
    resetContext();
  }

  @Test
  public void testCreatePermissions() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.createPermissions(Collections.singleton(permission));
    verify(permissionService).createPermissions(Collections.singleton(permission));
    resetContext();
  }

  @Test
  public void testUpdatePermission() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.updatePermission(permission);
    verify(permissionService).updatePermission(permission);
    resetContext();
  }

  @Test
  public void testUpdatePermissions() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    Permission permission = Permission.create(objectIdentity, sid, PermissionSet.WRITE);
    permissionServiceDecorator.updatePermissions(Collections.singleton(permission));
    verify(permissionService).updatePermissions(Collections.singleton(permission));
    resetContext();
  }

  @Test
  public void testDeletePermission() {
    setSu();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");

    MutableAcl acl = mock(MutableAcl.class);
    when(acl.getOwner()).thenReturn(sid);
    when(mutableAclService.readAclById(objectIdentity)).thenReturn(acl);

    permissionServiceDecorator.deletePermission(sid, objectIdentity);
    verify(permissionService).deletePermission(sid, objectIdentity);
    resetContext();
  }

  @Test
  public void testAddType() {
    setSu();
    permissionServiceDecorator.addType("entity-typeId");
    verify(permissionService).addType("entity-typeId");
    resetContext();
  }

  @Test
  public void testDeleteType() {
    setSu();
    permissionServiceDecorator.deleteType("entity-typeId");
    verify(permissionService).deleteType("entity-typeId");
    resetContext();
  }

  @Test
  public void testGetPermissionsForType() {
    setUser();
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissionsForType(
        "entity-typeId", Collections.singleton(sid), false);
    verify(permissionService)
        .getPermissionsForType("entity-typeId", Collections.singleton(sid), false);
    resetContext();
  }

  @Test
  public void testGetPermissionsForType1() {
    setUser();
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissionsForType(
        "entity-typeId", Collections.singleton(sid), 10, 10);
    verify(permissionService)
        .getPermissionsForType("entity-typeId", Collections.singleton(sid), 10, 10);
    resetContext();
  }

  @Test
  public void testGetPermissions() {
    setUser();
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.getPermissions(Collections.singleton(sid), false);
    verify(permissionService).getPermissions(Collections.singleton(sid), false);
    resetContext();
  }

  @Test
  public void testGetObjects() {
    setSu();
    permissionServiceDecorator.getObjects("entity-typeId", 10, 10);
    verify(permissionService).getObjects("entity-typeId", 10, 10);
    resetContext();
  }

  @Test
  public void testGetSuitablePermissionsForType() {
    setUser();
    when(entityHelper.getEntityTypeIdFromType("entity-typeId")).thenReturn("typeId");
    when(userPermissionEvaluator.hasPermission(new EntityTypeIdentity("typeId"), READ_METADATA))
        .thenReturn(true);
    permissionServiceDecorator.getSuitablePermissionsForType("entity-typeId");
    verify(permissionService).getSuitablePermissionsForType("entity-typeId");
  }

  @Test
  public void testExists() {
    setUser();
    ObjectIdentity objectIdentity = new ObjectIdentityImpl("type", "identifier");
    Sid sid = new PrincipalSid("user");
    permissionServiceDecorator.exists(objectIdentity, sid);
    verify(permissionService).exists(objectIdentity, sid);
    resetContext();
  }

  @AfterClass
  public void tearDown() {
    resetContext();
  }
}
