package org.molgenis.metadata.manager.mapper;

import static com.google.common.collect.ImmutableList.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.Sort;
import org.molgenis.metadata.manager.model.EditorOrder;
import org.molgenis.metadata.manager.model.EditorSort;

class SortMapperTest {
  private SortMapper sortMapper;

  @BeforeEach
  void setUp() {
    sortMapper = new SortMapper();
  }

  @Test
  void testToSort() {
    String attributeName = "attr";
    String direction = Sort.Direction.DESC.name();
    EditorSort editorSort = EditorSort.create(of(EditorOrder.create(attributeName, direction)));
    Sort sort = sortMapper.toSort(editorSort);
    Iterator<Sort.Order> iterator = sort.iterator();
    assertTrue(iterator.hasNext());
    Sort.Order order = iterator.next();
    assertEquals(order.getAttr(), attributeName);
    assertEquals(order.getDirection(), Sort.Direction.DESC);
    assertFalse(iterator.hasNext());
  }

  @Test
  void testToSortNull() {
    assertNull(sortMapper.toSort(null));
  }

  @Test
  void testToEditorSort() {
    String attr = "attr";
    Sort sort = new Sort(of(new Sort.Order(attr, Sort.Direction.ASC)));
    EditorSort editorSort = sortMapper.toEditorSort(sort);
    assertEquals(editorSort, EditorSort.create(of(EditorOrder.create(attr, "ASC"))));
  }

  @Test
  void testToEditorSortNull() {
    assertNull(sortMapper.toEditorSort(null));
  }
}
