package org.molgenis.data.importer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_NAME;
import static org.molgenis.data.importer.emx.EmxMetadataParser.EMX_PACKAGE_PARENT;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import org.molgenis.data.Entity;
import org.molgenis.data.importer.emx.exception.MissingRootPackageException;
import org.molgenis.data.importer.emx.exception.PackageResolveException;
import org.testng.annotations.Test;

public class PackageResolverTest {

  @Test
  public void testResolvePackages() {
    Entity parent = mock(Entity.class);
    when(parent.getString(EMX_PACKAGE_NAME)).thenReturn("parent");
    when(parent.getString(EMX_PACKAGE_PARENT)).thenReturn("");
    Entity middle = mock(Entity.class);
    when(middle.getString(EMX_PACKAGE_NAME)).thenReturn("parent_middle");
    when(middle.getString(EMX_PACKAGE_PARENT)).thenReturn("parent");
    Entity child = mock(Entity.class);
    when(child.getString(EMX_PACKAGE_NAME)).thenReturn("parent_middle_child");
    when(child.getString(EMX_PACKAGE_PARENT)).thenReturn("parent_middle");
    Entity parent2 = mock(Entity.class);
    when(parent2.getString(EMX_PACKAGE_NAME)).thenReturn("parent2");
    when(parent2.getString(EMX_PACKAGE_PARENT)).thenReturn(null);

    List packageRepo = Arrays.asList(parent, middle, child, parent2);
    assertTrue(PackageResolver.resolvePackages(packageRepo).containsAll(packageRepo));
  }

  @Test(expectedExceptions = PackageResolveException.class)
  public void testResolvePackagesUnresolvable() {
    Entity parent = mock(Entity.class);
    when(parent.getString(EMX_PACKAGE_NAME)).thenReturn("parent");
    when(parent.getString(EMX_PACKAGE_PARENT)).thenReturn("");
    Entity middle = mock(Entity.class);
    when(middle.getString(EMX_PACKAGE_NAME)).thenReturn("parent_middle");
    when(middle.getString(EMX_PACKAGE_PARENT)).thenReturn("parent_middle_child");
    Entity child = mock(Entity.class);
    when(child.getString(EMX_PACKAGE_NAME)).thenReturn("parent_middle_child");
    when(child.getString(EMX_PACKAGE_PARENT)).thenReturn("parent_middle");

    List packageRepo = Arrays.asList(parent, middle, child);
    PackageResolver.resolvePackages(packageRepo).containsAll(packageRepo);
  }

  @Test(expectedExceptions = MissingRootPackageException.class)
  public void testResolvePackagesNoRoot() {
    Entity middle = mock(Entity.class);
    when(middle.getString(EMX_PACKAGE_NAME)).thenReturn("parent_middle");
    when(middle.getString(EMX_PACKAGE_PARENT)).thenReturn("parent");
    Entity child = mock(Entity.class);
    when(child.getString(EMX_PACKAGE_NAME)).thenReturn("parent_middle_child");
    when(child.getString(EMX_PACKAGE_PARENT)).thenReturn("parent_middle");

    List packageRepo = Arrays.asList(middle, child);
    PackageResolver.resolvePackages(packageRepo).containsAll(packageRepo);
  }
}
