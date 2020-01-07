package org.molgenis.api.data;

import org.molgenis.api.model.Order;
import org.molgenis.data.Sort;
import org.molgenis.data.Sort.Direction;
import org.molgenis.data.UnknownSortAttributeException;
import org.molgenis.data.meta.model.EntityType;

public class SortMapper {
  SortMapper() {}

  /**
   * @param sort api-sort that will be mapped to a data-sort
   * @param entityType data-sort entity type
   * @throws UnknownSortAttributeException if api-sort contains an unknown entity type attribute
   */
  public Sort map(org.molgenis.api.model.Sort sort, EntityType entityType) {
    Sort newSort = new Sort();
    sort.getOrders()
        .forEach(
            order -> {
              String attributeName = order.getId();
              if (entityType.getAttribute(attributeName) == null) {
                throw new UnknownSortAttributeException(entityType, attributeName);
              }

              Order.Direction direction = order.getDirection();
              newSort.on(
                  attributeName,
                  direction != null ? Direction.valueOf(direction.name()) : Direction.ASC);
            });
    return newSort;
  }
}
