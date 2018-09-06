package org.molgenis.security.core;

import static org.molgenis.security.core.PermissionSet.*;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PermissionRegistryTest {
  private PermissionRegistry permissionRegistry = new PermissionRegistry();

  private enum TestPermission implements Permission {
    READ,
    UPDATE,
    DELETE;

    @Override
    public String getDefaultDescription() {
      return toString();
    }
  }

  @BeforeClass
  public void setUp() {
    permissionRegistry.addMapping(TestPermission.READ, READ, WRITE, WRITEMETA);
    permissionRegistry.addMapping(TestPermission.UPDATE, WRITE, WRITEMETA);
    permissionRegistry.addMapping(TestPermission.DELETE, WRITE, WRITEMETA);
  }

  @Test
  public void testGetPermissions() {
    assertEquals(
        permissionRegistry.getPermissions(TestPermission.UPDATE),
        ImmutableSet.of(WRITE, WRITEMETA));
  }

  @Test
  public void testGetPermissionSets() {
    Map<PermissionSet, Set<Permission>> expected =
        ImmutableMap.of(
            WRITEMETA,
            ImmutableSet.of(TestPermission.READ, TestPermission.UPDATE, TestPermission.DELETE),
            WRITE,
            ImmutableSet.of(TestPermission.READ, TestPermission.UPDATE, TestPermission.DELETE),
            READ,
            ImmutableSet.of(TestPermission.READ));
    assertEquals(permissionRegistry.getPermissionSets(), expected);
  }
}
