package org.molgenis.api.permissions.inheritance;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.api.permissions.inheritance.InheritanceTestUtils.getInheritedPermissionsResult;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.mockito.Mock;
import org.molgenis.api.permissions.PermissionTestUtils;
import org.molgenis.api.permissions.exceptions.InvalidTypeIdException;
import org.molgenis.api.permissions.inheritance.model.InheritedPermissionsResult;
import org.molgenis.api.permissions.model.response.InheritedPermission;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.testng.annotations.Test;

// Check InheritanceTestUtils for a description of the test setup
public class PermissionInheritanceResolverTest extends AbstractMolgenisSpringTest {

  @Mock DataService dataService;
  @Mock UserRoleInheritanceResolver userRoleInheritanceResolver;

  @Test
  public void testGetInheritedPermissions() {
    Sid user = mock(PrincipalSid.class);
    Sid role1Sid = new GrantedAuthoritySid("ROLE_role1");
    Sid role2Sid = new GrantedAuthoritySid("ROLE_role2");
    Sid role3Sid = new GrantedAuthoritySid("ROLE_role3");

    // Acl setup
    Acl parentPackageAcl =
        PermissionTestUtils.getSinglePermissionAcl(role3Sid, 16, "parentPackageAcl");
    Acl packageAcl =
        PermissionTestUtils.getSinglePermissionAcl(user, 4, "packageAcl", parentPackageAcl);
    Acl entityAcl =
        PermissionTestUtils.getSinglePermissionAcl(role2Sid, 8, "entityAcl", packageAcl);

    doReturn(Arrays.asList(role1Sid, role2Sid))
        .when(userRoleInheritanceResolver)
        .getRolesForSid(user);
    doReturn(Arrays.asList(role3Sid)).when(userRoleInheritanceResolver).getRolesForSid(role1Sid);

    InheritedPermissionsResult expected =
        getInheritedPermissionsResult(
            entityAcl, packageAcl, parentPackageAcl, role1Sid, role2Sid, role3Sid);
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getInheritedPermissions(entityAcl, user), expected);
  }

  @Test
  public void testConvertToInheritedPermissions() {
    Sid role1Sid = new GrantedAuthoritySid("ROLE_role1");
    Sid role2Sid = new GrantedAuthoritySid("ROLE_role2");
    Sid role3Sid = new GrantedAuthoritySid("ROLE_role3");
    Acl parentPackageAcl = mock(Acl.class);
    ObjectIdentity parentPackageObjectIdentity = mock(ObjectIdentity.class);
    when(parentPackageObjectIdentity.getIdentifier()).thenReturn("parent");
    when(parentPackageObjectIdentity.getType()).thenReturn("package");
    when(parentPackageAcl.getObjectIdentity()).thenReturn(parentPackageObjectIdentity);
    Acl packageAcl = mock(Acl.class);
    ObjectIdentity packageObjectIdentity = mock(ObjectIdentity.class);
    when(packageObjectIdentity.getIdentifier()).thenReturn("pack");
    when(packageObjectIdentity.getType()).thenReturn("package");
    when(packageAcl.getObjectIdentity()).thenReturn(packageObjectIdentity);
    Acl entityAcl = mock(Acl.class);

    InheritedPermissionsResult input =
        getInheritedPermissionsResult(
            entityAcl, packageAcl, parentPackageAcl, role1Sid, role2Sid, role3Sid);
    DataService dataService = mock(DataService.class);
    Repository<Entity> packageRepo = mock(Repository.class);

    EntityType entityType = mock(EntityType.class);
    when(entityType.getLabel()).thenReturn("Package");
    doReturn(entityType).when(packageRepo).getEntityType();
    Entity pack = mock(Entity.class);
    when(pack.getLabelValue()).thenReturn("Pack");
    doReturn(pack).when(packageRepo).findOneById("pack");
    Entity parent = mock(Entity.class);
    when(parent.getLabelValue()).thenReturn("Parent");
    doReturn(parent).when(packageRepo).findOneById("parent");
    doReturn(packageRepo).when(dataService).getRepository("sys_md_Package");

    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    List<InheritedPermission> actual = resolver.convertToInheritedPermissions(input);

    // expected
    InheritedPermission role3Permission =
        InheritedPermission.create(
            "role3", null, null, null, null, "WRITEMETA", Collections.emptyList());
    InheritedPermission role1Permission =
        InheritedPermission.create(
            "role1", null, null, null, null, null, Collections.singletonList(role3Permission));
    InheritedPermission parentPermission =
        InheritedPermission.create(
            null,
            "package",
            "Package",
            "parent",
            "Parent",
            null,
            Collections.singletonList(role1Permission));
    InheritedPermission packPermission =
        InheritedPermission.create(
            null,
            "package",
            "Package",
            "pack",
            "Pack",
            "READ",
            Collections.singletonList(parentPermission));
    InheritedPermission role2Permission =
        InheritedPermission.create(
            "role2", null, null, null, null, "WRITE", Collections.emptyList());

    List<InheritedPermission> expected = Arrays.asList(role2Permission, packPermission);

    assertEquals(actual, expected);
  }

  @Test
  public void testGetEntityTypeIdFromClassPlugin() {
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getEntityTypeIdFromClass("plugin"), "sys_Plugin");
  }

  @Test
  public void testGetEntityTypeIdFromClassET() {
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getEntityTypeIdFromClass("entityType"), "sys_md_EntityType");
  }

  @Test
  public void testGetEntityTypeIdFromClassGroup() {
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getEntityTypeIdFromClass("group"), "sys_sec_Group");
  }

  @Test
  public void testGetEntityTypeIdFromClassPack() {
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getEntityTypeIdFromClass("package"), "sys_md_Package");
  }

  @Test
  public void testGetEntityTypeIdFromClassEntity() {
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getEntityTypeIdFromClass("entity-test"), "test");
  }

  @Test(expectedExceptions = InvalidTypeIdException.class)
  public void testGetEntityTypeIdFromClassError() {
    PermissionInheritanceResolver resolver =
        new PermissionInheritanceResolver(dataService, userRoleInheritanceResolver);
    assertEquals(resolver.getEntityTypeIdFromClass("error"), "");
  }
}
