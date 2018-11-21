package org.molgenis.data.util;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.molgenis.data.meta.model.Package;
import org.testng.annotations.Test;

public class PackageUtilsTest {

  @Test
  public void isSystemPackageFalse() {
    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn("notSystem");
    assertFalse(PackageUtils.isSystemPackage(package_));
  }

  @Test
  public void isSystemPackageTrue() {
    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn(PACKAGE_SYSTEM);
    assertTrue(PackageUtils.isSystemPackage(package_));
  }

  @Test
  public void isSystemPackageTrueNested() {
    Package rootPackage_ = mock(Package.class);
    when(rootPackage_.getId()).thenReturn(PACKAGE_SYSTEM);

    Package package_ = mock(Package.class);
    when(package_.getId()).thenReturn("systemChild");
    when(package_.getRootPackage()).thenReturn(rootPackage_);
    assertTrue(PackageUtils.isSystemPackage(package_));
  }

  @Test
  public void testContains() {
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
  public void testNotContains() {
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
  public void testContainsIsItself() {
    Package packageA = mock(Package.class);

    assertTrue(PackageUtils.contains(packageA, packageA));
  }
}
