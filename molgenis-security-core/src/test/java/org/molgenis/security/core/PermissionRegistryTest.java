package org.molgenis.security.core;

import static com.google.common.collect.ImmutableSet.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PermissionRegistryTest {
  private static PermissionRegistry permissionRegistry = new PermissionRegistry();

  private enum TestPermission implements Permission {
    READ,
    UPDATE,
    DELETE;

    @Override
    public String getDefaultDescription() {
      return toString();
    }
  }

  @BeforeAll
  static void setUp() {
    permissionRegistry.addMapping(TestPermission.READ, READ, WRITE, WRITEMETA);
    permissionRegistry.addMapping(TestPermission.UPDATE, WRITE, WRITEMETA);
    permissionRegistry.addMapping(TestPermission.DELETE, WRITE, WRITEMETA);
  }

  @Test
  void testGetPermissions() {
    assertEquals(of(WRITE, WRITEMETA), permissionRegistry.getPermissions(TestPermission.UPDATE));
  }

  @Test
  void testGetPermissionSets() {
    Map<PermissionSet, Set<Permission>> expected =
        ImmutableMap.of(
            WRITEMETA,
            ImmutableSet.of(TestPermission.READ, TestPermission.UPDATE, TestPermission.DELETE),
            WRITE,
            ImmutableSet.of(TestPermission.READ, TestPermission.UPDATE, TestPermission.DELETE),
            READ,
            ImmutableSet.of(TestPermission.READ));
    assertEquals(expected, permissionRegistry.getPermissionSets());
  }
}
