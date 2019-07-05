package org.molgenis.api.data.v3;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import org.molgenis.api.model.Sort;
import org.molgenis.api.model.Sort.Order;
import org.molgenis.api.model.Sort.Order.Direction;
import org.testng.annotations.Test;

public class SortV3MapperTest {

  @Test
  public void testMap() {
    Sort sort =
        Sort.create(
            asList(Order.create("attr1", Direction.ASC), Order.create("attr2", Direction.DESC)));
    org.molgenis.data.Sort expected = new org.molgenis.data.Sort();
    expected.on("attr1", org.molgenis.data.Sort.Direction.ASC);
    expected.on("attr2", org.molgenis.data.Sort.Direction.DESC);
    assertEquals(SortV3Mapper.map(sort), expected);
  }
}
