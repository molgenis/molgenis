package org.molgenis.data.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.permission.PermissionSetUtils.paramValueToPermissionSet;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.molgenis.data.security.permission.model.LabelledPermission;
import org.molgenis.security.core.PermissionSet;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Permission;

class PermissionSetUtilsTest extends AbstractMockitoTest {
  @Test
  void testParamValueToPermissionSetRead() {
    assertEquals(READ, paramValueToPermissionSet("read"));
  }

  @Test
  void testParamValueToPermissionSetWrite() {
    assertEquals(WRITE, paramValueToPermissionSet("write"));
  }

  @Test
  void testParamValueToPermissionSetWritemeta() {
    assertEquals(WRITEMETA, paramValueToPermissionSet("writemeta"));
  }

  @Test
  void testParamValueToPermissionSetReadmeta() {
    assertEquals(READMETA, paramValueToPermissionSet("readmeta"));
  }

  @Test
  void testParamValueToPermissionSetCount() {
    assertEquals(COUNT, paramValueToPermissionSet("count"));
  }

  @Test
  void testParamValueToPermissionSetInvalid() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> PermissionSetUtils.paramValueToPermissionSet("test"));
    assertThat(exception.getMessage()).containsPattern("Unknown PermissionSet 'test'");
  }

  static Iterator<Object[]> testGetPermissionStringValueProvider() {
    List<Object[]> dataItems = new ArrayList<>(5);
    dataItems.add(new Object[] {1, "READMETA"});
    dataItems.add(new Object[] {2, "COUNT"});
    dataItems.add(new Object[] {4, "READ"});
    dataItems.add(new Object[] {8, "WRITE"});
    dataItems.add(new Object[] {16, "WRITEMETA"});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("testGetPermissionStringValueProvider")
  void testGetPermissionStringValueLabelledPermission(int mask, String stringValue) {
    LabelledPermission labelledPermission = mock(LabelledPermission.class);
    PermissionSet permissionSet = new PermissionSet("name", mask, 'x');
    when(labelledPermission.getPermission()).thenReturn(permissionSet);

    assertEquals(
        Optional.of(stringValue), PermissionSetUtils.getPermissionStringValue(labelledPermission));
  }

  @Test
  void testGetPermissionStringValueLabelledPermissionNull() {
    LabelledPermission labelledPermission = mock(LabelledPermission.class);
    assertEquals(Optional.empty(), PermissionSetUtils.getPermissionStringValue(labelledPermission));
  }

  @ParameterizedTest
  @MethodSource("testGetPermissionStringValueProvider")
  void testGetPermissionStringValue(int mask, String stringValue) {
    PermissionSet permissionSet = new PermissionSet("name", mask, 'x');
    assertEquals(stringValue, PermissionSetUtils.getPermissionStringValue(permissionSet));
  }

  @Test
  void testGetPermissionStringValueInvalidMask() {
    PermissionSet permissionSet = new PermissionSet("name", 32, 'x');
    assertThrows(
        IllegalArgumentException.class,
        () -> PermissionSetUtils.getPermissionStringValue(permissionSet));
  }

  static Iterator<Object[]> testGetPermissionSetProvider() {
    List<Object[]> dataItems = new ArrayList<>(5);
    dataItems.add(new Object[] {1, READMETA});
    dataItems.add(new Object[] {2, COUNT});
    dataItems.add(new Object[] {4, READ});
    dataItems.add(new Object[] {8, WRITE});
    dataItems.add(new Object[] {16, WRITEMETA});
    return dataItems.iterator();
  }

  @ParameterizedTest
  @MethodSource("testGetPermissionSetProvider")
  void testGetPermissionSet(int mask, PermissionSet permissionSet) {
    AccessControlEntry accessControlEntry = mock(AccessControlEntry.class);
    Permission permission = when(mock(Permission.class).getMask()).thenReturn(mask).getMock();
    when(accessControlEntry.getPermission()).thenReturn(permission);
    assertEquals(permissionSet, PermissionSetUtils.getPermissionSet(accessControlEntry));
  }

  @Test
  void testGetPermissionSetInvalidMask() {
    AccessControlEntry accessControlEntry = mock(AccessControlEntry.class);
    Permission permission = when(mock(Permission.class).getMask()).thenReturn(32).getMock();
    when(accessControlEntry.getPermission()).thenReturn(permission);
    assertThrows(
        IllegalArgumentException.class,
        () -> PermissionSetUtils.getPermissionSet(accessControlEntry));
  }
}
