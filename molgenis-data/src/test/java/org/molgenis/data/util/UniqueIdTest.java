package org.molgenis.data.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class UniqueIdTest {
  @Test
  void testGetId() {
    UniqueId uniqueId = new UniqueId();
    assertNotEquals(uniqueId.getId(), uniqueId.getId());
  }
}
