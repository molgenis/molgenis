package org.molgenis.api.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order;
import org.molgenis.api.model.Sort.Order.Direction;
import org.molgenis.data.UnknownSortAttributeException;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

class SortMapperTest {

  @Test
  void testMap() {
    Attribute attr1 = mock(Attribute.class);
    Attribute attr2 = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    doReturn(attr1).when(entityType).getAttribute("attr1");
    doReturn(attr2).when(entityType).getAttribute("attr2");

    SortMapper sortMapper = new SortMapper();
    Sort sort =
        Sort.create(
            asList(Order.create("attr1", Direction.ASC), Order.create("attr2", Direction.DESC)));
    org.molgenis.data.Sort expected = new org.molgenis.data.Sort();
    expected.on("attr1", org.molgenis.data.Sort.Direction.ASC);
    expected.on("attr2", org.molgenis.data.Sort.Direction.DESC);
    assertEquals(expected, sortMapper.map(sort, entityType));
  }

  @Test
  void testMapNoOrder() {
    Attribute attr1 = mock(Attribute.class);
    EntityType entityType = mock(EntityType.class);
    doReturn(attr1).when(entityType).getAttribute("attr1");

    SortMapper sortMapper = new SortMapper();
    Sort sort = Sort.create(singletonList(Order.create("attr1")));
    org.molgenis.data.Sort expected =
        new org.molgenis.data.Sort().on("attr1", org.molgenis.data.Sort.Direction.ASC);
    assertEquals(expected, sortMapper.map(sort, entityType));
  }

  @Test
  void testMapUnknownAttribute() {
    SortMapper sortMapper = new SortMapper();
    Sort sort = Sort.create(singletonList(Order.create("attr1")));
    EntityType entityType = mock(EntityType.class);
    assertThrows(UnknownSortAttributeException.class, () -> sortMapper.map(sort, entityType));
  }
}
