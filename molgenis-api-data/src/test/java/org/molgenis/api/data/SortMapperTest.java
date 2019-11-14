package org.molgenis.api.data;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.api.data.SortMapper;
import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order;
import org.molgenis.api.model.Sort.Order.Direction;

class SortMapperTest {
  @Test
  void testMap() {
    SortMapper sortMapper = new SortMapper();
    Sort sort =
        Sort.create(
            asList(Order.create("attr1", Direction.ASC), Order.create("attr2", Direction.DESC)));
    org.molgenis.data.Sort expected = new org.molgenis.data.Sort();
    expected.on("attr1", org.molgenis.data.Sort.Direction.ASC);
    expected.on("attr2", org.molgenis.data.Sort.Direction.DESC);
    assertEquals(expected, sortMapper.map(sort));
  }

  @Test
  void testMapNoOrder() {
    SortMapper sortMapper = new SortMapper();
    Sort sort = Sort.create(singletonList(Order.create("attr1")));
    org.molgenis.data.Sort expected =
        new org.molgenis.data.Sort().on("attr1", org.molgenis.data.Sort.Direction.ASC);
    assertEquals(expected, sortMapper.map(sort));
  }
}
