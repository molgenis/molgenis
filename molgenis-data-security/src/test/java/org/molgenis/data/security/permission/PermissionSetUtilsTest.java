package org.molgenis.data.security.permission;

import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;
import static org.testng.Assert.assertEquals;

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

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "Unknown PermissionSet \'test\'")
  public void testParamValueToPermissionSetInvalid() {
    PermissionSetUtils.paramValueToPermissionSet("test");
  }
}
