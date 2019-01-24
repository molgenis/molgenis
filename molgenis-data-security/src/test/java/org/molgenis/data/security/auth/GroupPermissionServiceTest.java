package org.molgenis.data.security.auth;

import static org.mockito.Mockito.verify;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.molgenis.security.core.SidUtils.createRoleSid;

import org.mockito.Mock;
import org.molgenis.data.security.GroupIdentity;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.security.core.GroupValueFactoryTest;
import org.molgenis.security.core.PermissionService;
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
        .grant(new PackageIdentity("bbmri_eric"), WRITEMETA, createRoleSid("BBMRI_ERIC_MANAGER"));
    verify(aclService).createAcl(new GroupIdentity("bbmri-eric"));
    verify(permissionService)
        .grant(new GroupIdentity("bbmri-eric"), WRITEMETA, createRoleSid("BBMRI_ERIC_MANAGER"));
  }
}
