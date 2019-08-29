package org.molgenis.data.util;

import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

public class UniqueIdTest {
  @Test
  public void testGetId() {
    byte[] id0 = new UniqueId().getId();
    byte[] id1 = new UniqueId().getId();
    assertNotEquals(id0, id1);
  }

  @Test
  public void testGetIdReuse() {
    UniqueId uniqueId = new UniqueId();
    assertNotEquals(uniqueId.getId(), uniqueId.getId());
  }
}
