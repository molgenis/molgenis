package org.molgenis.api.data.v3;

import org.molgenis.api.model.Sort.Order;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;

public class SortV3Mapper {
  private SortV3Mapper() {}

  public static Sort map(org.molgenis.api.model.Sort sort) {
    Sort newSort = new Sort();
    sort.getOrders()
        .forEach(
            order -> {
              Order.Direction direction = order.getDirection();
              newSort.on(
                  order.getItem(),
                  direction != null ? Direction.valueOf(direction.name()) : Direction.ASC);
            });
    return newSort;
  }
}
