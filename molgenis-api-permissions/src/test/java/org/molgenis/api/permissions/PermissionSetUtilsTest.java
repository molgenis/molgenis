package org.molgenis.api.permissions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.testng.Assert.assertEquals;

import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Permission;
import org.testng.annotations.Test;

public class PermissionSetUtilsTest {

  @Test
  public void testParamValueToPermissionSetRead() {
    assertEquals(READ, PermissionSetUtils.paramValueToPermissionSet("read"));
  }

  @Test
  public void testParamValueToPermissionSetWrite() {
    assertEquals(WRITE, PermissionSetUtils.paramValueToPermissionSet("write"));
  }

  @Test
  public void testParamValueToPermissionSetWritemeta() {
    assertEquals(WRITEMETA, PermissionSetUtils.paramValueToPermissionSet("writemeta"));
  }

  @Test
  public void testParamValueToPermissionSetReadmeta() {
    assertEquals(READMETA, PermissionSetUtils.paramValueToPermissionSet("readmeta"));
  }

  @Test
  public void testParamValueToPermissionSetCount() {
    assertEquals(COUNT, PermissionSetUtils.paramValueToPermissionSet("count"));
  }

  @Test
  public void testGetPermissionStringValueRM() {
    assertEquals(
        "READMETA", PermissionSetUtils.getPermissionStringValue(getAccessControlEntryForMask(1)));
  }

  @Test
  public void testGetPermissionStringValueC() {
    assertEquals(
        "COUNT", PermissionSetUtils.getPermissionStringValue(getAccessControlEntryForMask(2)));
  }

  @Test
  public void testGetPermissionStringValueR() {
    assertEquals(
        "READ", PermissionSetUtils.getPermissionStringValue(getAccessControlEntryForMask(4)));
  }

  @Test
  public void testGetPermissionStringValueW() {
    assertEquals(
        "WRITE", PermissionSetUtils.getPermissionStringValue(getAccessControlEntryForMask(8)));
  }

  @Test
  public void testGetPermissionStringValueWM() {
    assertEquals(
        "WRITEMETA", PermissionSetUtils.getPermissionStringValue(getAccessControlEntryForMask(16)));
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Unexpected mask \'99\'")
  public void testGetPermissionStringValueInvalid() {
    PermissionSetUtils.getPermissionStringValue(getAccessControlEntryForMask(99));
  }

  private AccessControlEntry getAccessControlEntryForMask(int mask) {
    AccessControlEntry accessControlEntry = mock(AccessControlEntry.class);
    Permission permission = mock(Permission.class);
    when(permission.getMask()).thenReturn(mask);
    when(accessControlEntry.getPermission()).thenReturn(permission);
    return accessControlEntry;
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Unknown PermissionSet \'test\'")
  public void testParamValueToPermissionSetInvaliud() {
    PermissionSetUtils.paramValueToPermissionSet("test");
  }
}
