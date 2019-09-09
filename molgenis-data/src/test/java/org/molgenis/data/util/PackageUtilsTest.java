package org.molgenis.data.util;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Package;

class PackageUtilsTest {

  @Test
  void isSystemPackageFalse() {
    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn("notSystem");
    assertFalse(PackageUtils.isSystemPackage(package_));
  }

  @Test
  void isSystemPackageTrue() {
    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn(PACKAGE_SYSTEM);
    assertTrue(PackageUtils.isSystemPackage(package_));
  }

  @Test
  void isSystemPackageTrueNested() {
    Package rootPackage_ = mock(Package.class);
    when(rootPackage_.getId()).thenReturn(PACKAGE_SYSTEM);

    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn("systemChild");
    when(package_.getRootPackage()).thenReturn(rootPackage_);
    assertTrue(PackageUtils.isSystemPackage(package_));
  }

  @Test
  void testContains() {
    Package packageA = mock(Package.class);
    Package packageAa = mock(Package.class);
    Package packageAb = mock(Package.class);
    Package packageAba = mock(Package.class);
    Package packageC = mock(Package.class);
    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb, packageC));
    when(packageAb.getChildren()).thenReturn(singletonList(packageAba));

    assertTrue(PackageUtils.contains(packageA, packageAba));
  }

  @Test
  void testNotContains() {
    Package packageA = mock(Package.class);
    Package packageAa = mock(Package.class);
    Package packageAb = mock(Package.class);
    Package packageAba = mock(Package.class);
    Package packageC = mock(Package.class);
    when(packageA.getChildren()).thenReturn(asList(packageAa, packageAb, packageC));
    when(packageAb.getChildren()).thenReturn(singletonList(packageAba));

    assertFalse(PackageUtils.contains(packageA, mock(Package.class)));
  }

  @Test
  void testContainsIsItself() {
    Package packageA = mock(Package.class);

    assertTrue(PackageUtils.contains(packageA, packageA));
  }
}
