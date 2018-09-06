package org.molgenis.data.security;

import static org.mockito.Mockito.*;
import static org.molgenis.data.security.PackagePermission.ADD_ENTITY_TYPE;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.mockito.Mock;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class PackagePermissionUtilsTest extends AbstractMockitoTest {
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @Test
  public void testIsWritablePackageAddPackagePermission() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new PackageIdentity("packageId"), ADD_PACKAGE);
    doReturn(false)
        .when(userPermissionEvaluator)
        .hasPermission(new PackageIdentity("packageId"), ADD_ENTITY_TYPE);
    assertTrue(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }

  @Test
  public void testIsWritablePackageAddEntityTypePermission() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new PackageIdentity("packageId"), ADD_ENTITY_TYPE);
    assertTrue(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }

  @Test
  public void testIsWritablePackageFalse() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    assertFalse(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }

  @Test
  public void testIsWritablePackageSystemPackage() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    when(aPackage.getId()).thenReturn("sys");
    assertFalse(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }
}
