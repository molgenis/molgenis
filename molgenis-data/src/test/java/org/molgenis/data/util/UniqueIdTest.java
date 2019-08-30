package org.molgenis.data.util;

import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

public class UniqueIdTest {
  @Test
  public void testGetId() {
    UniqueId uniqueId = new UniqueId();
    assertNotEquals(uniqueId.getId(), uniqueId.getId());
  }
}
