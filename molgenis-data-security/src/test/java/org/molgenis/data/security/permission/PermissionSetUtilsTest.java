package org.molgenis.data.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;

import org.junit.jupiter.api.Test;

class PermissionSetUtilsTest {

  @Test
  void testParamValueToPermissionSetRead() {
    assertEquals(READ, PermissionSetUtils.paramValueToPermissionSet("read"));
  }

  @Test
  void testParamValueToPermissionSetWrite() {
    assertEquals(WRITE, PermissionSetUtils.paramValueToPermissionSet("write"));
  }

  @Test
  void testParamValueToPermissionSetWritemeta() {
    assertEquals(WRITEMETA, PermissionSetUtils.paramValueToPermissionSet("writemeta"));
  }

  @Test
  void testParamValueToPermissionSetReadmeta() {
    assertEquals(READMETA, PermissionSetUtils.paramValueToPermissionSet("readmeta"));
  }

  @Test
  void testParamValueToPermissionSetCount() {
    assertEquals(COUNT, PermissionSetUtils.paramValueToPermissionSet("count"));
  }

  @Test
  void testParamValueToPermissionSetInvalid() {
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> PermissionSetUtils.paramValueToPermissionSet("test"));
    assertThat(exception.getMessage()).containsPattern("Unknown PermissionSet \'test\'");
  }
}
