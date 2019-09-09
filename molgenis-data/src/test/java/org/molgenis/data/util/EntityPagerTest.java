package org.molgenis.data.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.molgenis.data.support.DynamicEntity;

class EntityPagerTest {
  @Test
  void getNextStart() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(2, 5, 10, null);
    assertEquals(entityPager.getNextStart(), Integer.valueOf(7));
  }

  @Test
  void getNextStart_limit() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(2, 5, 4, null);
    assertNull(entityPager.getNextStart());
  }

  @Test
  void getNextStart_borderLeft() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(0, 3, 10, null);
    assertEquals(entityPager.getNextStart(), Integer.valueOf(3));
  }

  @Test
  void getNextStart_borderRight() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(0, 1, 2, null);
    assertEquals(entityPager.getNextStart(), Integer.valueOf(1));
  }

  @Test
  void getPrevStart() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(8, 5, 10, null);
    assertEquals(entityPager.getPrevStart(), Integer.valueOf(3));
  }

  @Test
  void getPrevStart_offset() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(0, 3, 10, null);
    assertNull(entityPager.getPrevStart());
  }

  @Test
  void getPrevStart_borderLeft() {
    EntityPager<DynamicEntity> entityPager = new EntityPager<>(3, 3, 10, null);
    assertEquals(entityPager.getPrevStart(), Integer.valueOf(0));
  }
}
