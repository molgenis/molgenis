package org.molgenis.metadata.manager.mapper;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.molgenis.data.Sort;
import org.molgenis.metadata.manager.model.EditorOrder;
import org.molgenis.metadata.manager.model.EditorSort;
import org.springframework.stereotype.Component;

@Component
class SortMapper {
  Sort toSort(EditorSort editorSort) {
    if (editorSort == null) {
      return null;
    }

    List<Sort.Order> orders = editorSort.getOrders().stream().map(this::toOrder).collect(toList());
    return new Sort(orders);
  }

  private Sort.Order toOrder(EditorOrder editorOrder) {
    if (editorOrder == null) {
      return null;
    }
    return new Sort.Order(editorOrder.getAttributeName(), toDirection(editorOrder.getDirection()));
  }

  private Sort.Direction toDirection(String editorDirection) {
    if (editorDirection == null) {
      return null;
    }
    return Sort.Direction.valueOf(editorDirection);
  }

  EditorSort toEditorSort(Sort sort) {
    if (sort == null) {
      return null;
    }

    return EditorSort.create(toEditorOrders(sort));
  }

  private ImmutableList<EditorOrder> toEditorOrders(Iterable<Sort.Order> orders) {
    return ImmutableList.copyOf(
        stream(orders.spliterator(), false).map(this::toEditorOrder).iterator());
  }

  private EditorOrder toEditorOrder(Sort.Order order) {
    return EditorOrder.create(order.getAttr(), order.getDirection().toString());
  }
}
