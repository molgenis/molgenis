package org.molgenis.api.data.v3;

import static org.testng.Assert.*;

import org.molgenis.api.data.v3.Sort.Direction;
import org.testng.annotations.Test;

public class SortV3MapperTest {

  @Test
  public void testMap() {
    Sort sort = new Sort();
    sort.on("attr1", Direction.ASC);
    sort.on("attr2", Direction.DESC);
    org.molgenis.data.Sort expected = new org.molgenis.data.Sort();
    expected.on("attr1", org.molgenis.data.Sort.Direction.ASC);
    expected.on("attr2", org.molgenis.data.Sort.Direction.DESC);
    assertEquals(SortV3Mapper.map(sort), expected);
  }
}
