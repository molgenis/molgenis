package org.molgenis.data.security.auth;

import static org.mockito.Mockito.verify;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.molgenis.security.core.SidUtils.createRoleSid;

import org.mockito.Mock;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.PermissionService;
import org.molgenis.data.security.permission.model.Permission;
import org.molgenis.security.core.GroupValueFactoryTest;
import org.molgenis.security.core.model.GroupValue;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GroupPermissionServiceTest extends AbstractMockitoTest {
  private GroupValue groupValue;
  private GroupPermissionService groupPermissionService;

  @Mock private PermissionService permissionService;

  @Mock private MutableAclService aclService;

  @BeforeMethod
  public void beforeMethod() {
    groupValue = GroupValueFactoryTest.getTestGroupValue();
    groupPermissionService = new GroupPermissionService(aclService, permissionService);
  }

  @Test
  public void testGrantPermissions() {
    groupPermissionService.grantDefaultPermissions(groupValue);

    verify(permissionService)
        .createPermission(
            Permission.create(
                new PackageIdentity("bbmri_eric"), createRoleSid("BBMRI_ERIC_MANAGER"), WRITEMETA));
    verify(aclService).createAcl(new GroupIdentity("bbmri-eric"));
    verify(permissionService)
        .createPermission(
            Permission.create(
                new GroupIdentity("bbmri-eric"), createRoleSid("BBMRI_ERIC_MANAGER"), WRITEMETA));
  }
}
