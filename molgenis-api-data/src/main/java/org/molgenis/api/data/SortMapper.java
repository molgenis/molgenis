package org.molgenis.api.data;

import org.molgenis.api.model.Sort.Order;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;

public class SortMapper {
  SortMapper() {}

  public Sort map(org.molgenis.api.model.Sort sort) {
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
