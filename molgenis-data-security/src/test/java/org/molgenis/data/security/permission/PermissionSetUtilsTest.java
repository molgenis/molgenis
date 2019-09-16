package org.molgenis.data.security.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.molgenis.data.security.permission.PermissionSetUtils.paramValueToPermissionSet;
import static org.molgenis.security.core.PermissionSet.COUNT;
import static org.molgenis.security.core.PermissionSet.READ;
import static org.molgenis.security.core.PermissionSet.READMETA;
import static org.molgenis.security.core.PermissionSet.WRITE;
import static org.molgenis.security.core.PermissionSet.WRITEMETA;

import org.junit.jupiter.api.Test;

class PermissionSetUtilsTest {

  @Test
  void testParamValueToPermissionSetRead() {
    assertEquals(paramValueToPermissionSet("read"), READ);
  }

  @Test
  void testParamValueToPermissionSetWrite() {
    assertEquals(paramValueToPermissionSet("write"), WRITE);
  }

  @Test
  void testParamValueToPermissionSetWritemeta() {
    assertEquals(paramValueToPermissionSet("writemeta"), WRITEMETA);
  }

  @Test
  void testParamValueToPermissionSetReadmeta() {
    assertEquals(paramValueToPermissionSet("readmeta"), READMETA);
  }

  @Test
  void testParamValueToPermissionSetCount() {
    assertEquals(paramValueToPermissionSet("count"), COUNT);
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
