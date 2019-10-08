package org.molgenis.data.security.permission.inheritance;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.permission.inheritance.InheritanceTestUtils.getInheritedPermissionsResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.security.permission.EntityHelper;
import org.molgenis.data.security.permission.PermissionTestUtils;
import org.molgenis.data.security.permission.UserRoleTools;
import org.molgenis.data.security.permission.inheritance.model.InheritedPermissionsResult;
import org.molgenis.data.security.permission.model.LabelledObjectIdentity;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

// Check InheritanceTestUtils for a description of the test setup
class PermissionInheritanceResolverTest extends AbstractMockitoTest {
  @Mock private UserRoleTools userRoleTools;
  @Mock private EntityHelper entityHelper;
  private PermissionInheritanceResolver resolver;

  @BeforeEach
  private void setUpBeforeMethod() {
    resolver = new PermissionInheritanceResolver(userRoleTools, entityHelper);
  }

  @Test
  void testGetInheritedPermissions() {
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

    doReturn(Arrays.asList(role1Sid, role2Sid)).when(userRoleTools).getRolesForSid(user);
    doReturn(singletonList(role3Sid)).when(userRoleTools).getRolesForSid(role1Sid);

    InheritedPermissionsResult expected =
        getInheritedPermissionsResult(packageAcl, parentPackageAcl, role1Sid, role2Sid, role3Sid);

    assertEquals(expected, resolver.getInheritedPermissionsResults(entityAcl, user));
  }

  @Test
  void testConvertToInheritedPermissions() {
    Sid role1Sid = new GrantedAuthoritySid("ROLE_role1");
    Sid role2Sid = new GrantedAuthoritySid("ROLE_role2");
    Sid role3Sid = new GrantedAuthoritySid("ROLE_role3");
    Acl parentPackageAcl = mock(Acl.class);
    ObjectIdentity parentPackageObjectIdentity = new ObjectIdentityImpl("package", "parent");
    LabelledObjectIdentity labelledParentPackageObjectIdentity =
        LabelledObjectIdentity.create("package", "sys_md_package", "package", "parent", "Parent");
    when(parentPackageAcl.getObjectIdentity()).thenReturn(parentPackageObjectIdentity);
    Acl packageAcl = mock(Acl.class);
    ObjectIdentity packageObjectIdentity = new ObjectIdentityImpl("package", "pack");
    LabelledObjectIdentity labelledPackageObjectIdentity =
        LabelledObjectIdentity.create("package", "sys_md_package", "package", "pack", "Pack");
    when(packageAcl.getObjectIdentity()).thenReturn(packageObjectIdentity);

    InheritedPermissionsResult input =
        getInheritedPermissionsResult(packageAcl, parentPackageAcl, role1Sid, role2Sid, role3Sid);
    @SuppressWarnings("unchecked")
    Repository<Entity> packageRepo = mock(Repository.class);

    doReturn(labelledPackageObjectIdentity)
        .when(entityHelper)
        .getLabelledObjectIdentity(packageObjectIdentity);
    doReturn(labelledParentPackageObjectIdentity)
        .when(entityHelper)
        .getLabelledObjectIdentity(parentPackageObjectIdentity);
    Set<LabelledPermission> actual = resolver.convertToInheritedPermissions(input);

    // expected
    LabelledPermission role3Permission =
        LabelledPermission.create(role3Sid, null, PermissionSet.WRITEMETA, Collections.emptySet());
    LabelledPermission role1Permission =
        LabelledPermission.create(role1Sid, null, null, singleton(role3Permission));
    LabelledPermission parentPermission =
        LabelledPermission.create(
            null,
            LabelledObjectIdentity.create(
                "package", "sys_md_package", "package", "parent", "Parent"),
            null,
            singleton(role1Permission));
    LabelledPermission packPermission =
        LabelledPermission.create(
            null,
            LabelledObjectIdentity.create("package", "sys_md_package", "package", "pack", "Pack"),
            PermissionSet.READ,
            singleton(parentPermission));
    LabelledPermission role2Permission =
        LabelledPermission.create(role2Sid, null, PermissionSet.WRITE, Collections.emptySet());

    Set<LabelledPermission> expected =
        new HashSet<>(Arrays.asList(role2Permission, packPermission));

    assertEquals(expected, actual);
  }
}
