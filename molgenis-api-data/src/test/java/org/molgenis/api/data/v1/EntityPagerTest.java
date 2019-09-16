package org.molgenis.api.data.v1;

import static java.lang.Integer.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class EntityPagerTest {

  @Test
  void getNextStart() {
    EntityPager pager = new EntityPager(10, 15, null, null);
    assertEquals(valueOf(25), pager.getNextStart());

    pager = new EntityPager(0, 10, 25L, null);
    assertEquals(valueOf(10), pager.getNextStart());

    pager = new EntityPager(10, 10, 25L, null);
    assertEquals(valueOf(20), pager.getNextStart());

    pager = new EntityPager(0, 25, 25L, null);
    assertNull(pager.getNextStart());
  }

  @Test
  void getPrevStart() {
    EntityPager pager = new EntityPager(10, 15, null, null);
    assertEquals(valueOf(0), pager.getPrevStart());

    pager = new EntityPager(0, 15, 30L, null);
    assertNull(pager.getPrevStart());

    pager = new EntityPager(15, 15, 30L, null);
    assertEquals(valueOf(0), pager.getPrevStart());

    pager = new EntityPager(30, 15, 30L, null);
    assertEquals(valueOf(15), pager.getPrevStart());
  }
}
