package org.molgenis.data.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.PackagePermission.ADD_ENTITY_TYPE;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Package;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.test.AbstractMockitoTest;

class PackagePermissionUtilsTest extends AbstractMockitoTest {
  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @Test
  void testIsWritablePackageAddPackagePermission() {
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
  void testIsWritablePackageAddEntityTypePermission() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new PackageIdentity("packageId"), ADD_ENTITY_TYPE);
    assertTrue(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }

  @Test
  void testIsWritablePackageFalse() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    assertFalse(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }

  @Test
  void testIsWritablePackageSystemPackage() {
    Package aPackage = when(mock(Package.class).getId()).thenReturn("packageId").getMock();
    when(aPackage.getId()).thenReturn("sys");
    assertFalse(PackagePermissionUtils.isWritablePackage(aPackage, userPermissionEvaluator));
  }
}
