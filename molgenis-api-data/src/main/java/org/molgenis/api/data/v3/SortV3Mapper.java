package org.molgenis.api.data.v3;

import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;

public class SortV3Mapper {
  public static Sort map(org.molgenis.api.data.v3.Sort sort) {
    Sort newSort = new Sort();
    sort.forEach(
        order -> newSort.on(order.getAttr(), Direction.valueOf(order.getDirection().name())));
    return newSort;
  }
}
