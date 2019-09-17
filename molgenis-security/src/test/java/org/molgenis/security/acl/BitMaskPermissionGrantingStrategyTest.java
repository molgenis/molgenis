package org.molgenis.security.acl;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.model.Permission;

class BitMaskPermissionGrantingStrategyTest {
  static Object[][] permissionsMatchProvider() {
    return new Object[][] {{READ, new CumulativePermission().set(READ).set(WRITE)}, {READ, READ}};
  }

  @ParameterizedTest
  @MethodSource("permissionsMatchProvider")
  void testPermissionsMatch(Permission acePermission, Permission testedPermission) {
    assertTrue(
        BitMaskPermissionGrantingStrategy.containsPermission(
            acePermission.getMask(), testedPermission.getMask()),
        format(
            "combined ACE permission %s should match tested permission %s",
            acePermission, testedPermission));
  }

  static Object[][] permissionsDontMatchProvider() {
    return new Object[][] {
      {READ, WRITE},
      {COUNT, new CumulativePermission().set(READ).set(WRITE).set(WRITEMETA)},
      {WRITE, new CumulativePermission().set(READ).set(WRITEMETA)}
    };
  }

  @ParameterizedTest
  @MethodSource("permissionsDontMatchProvider")
  void testPermissionsDontMatch(Permission acePermission, Permission testedPermission) {
    assertFalse(
        BitMaskPermissionGrantingStrategy.containsPermission(
            acePermission.getMask(), testedPermission.getMask()),
        format(
            "combined ACE permission %s should NOT match tested permission %s",
            acePermission, testedPermission));
  }
}
