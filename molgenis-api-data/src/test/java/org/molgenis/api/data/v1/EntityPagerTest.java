package org.molgenis.api.data.v1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class EntityPagerTest {

  @Test
  void getNextStart() {
    EntityPager pager = new EntityPager(10, 15, null, null);
    assertEquals(pager.getNextStart(), Integer.valueOf(25));

    pager = new EntityPager(0, 10, 25L, null);
    assertEquals(pager.getNextStart(), Integer.valueOf(10));

    pager = new EntityPager(10, 10, 25L, null);
    assertEquals(pager.getNextStart(), Integer.valueOf(20));

    pager = new EntityPager(0, 25, 25L, null);
    assertNull(pager.getNextStart());
  }

  @Test
  void getPrevStart() {
    EntityPager pager = new EntityPager(10, 15, null, null);
    assertEquals(pager.getPrevStart(), Integer.valueOf(0));

    pager = new EntityPager(0, 15, 30L, null);
    assertNull(pager.getPrevStart());

    pager = new EntityPager(15, 15, 30L, null);
    assertEquals(pager.getPrevStart(), Integer.valueOf(0));

    pager = new EntityPager(30, 15, 30L, null);
    assertEquals(pager.getPrevStart(), Integer.valueOf(15));
  }
}
