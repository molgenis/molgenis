package org.molgenis.api.data.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class Sort implements Iterable<Sort.Order> {
  private final List<Order> orders;

  public Sort() {
    this(new ArrayList<>());
  }

  public Sort(String attr) {
    this(attr, Direction.ASC);
  }

  public Sort(String attr, Direction direction) {
    this(Collections.singletonList(new Sort.Order(attr, direction)));
  }

  public Sort(List<Sort.Order> orders) {
    this.orders = orders != null ? new ArrayList<>(orders) : new ArrayList<>();
  }

  public Sort(Sort sort) {
    orders = new ArrayList<>(sort.orders.size());
    for (Sort.Order order : sort) {
      orders.add(new Order(order.getAttr(), order.getDirection()));
    }
  }

  @Override
  public Iterator<Order> iterator() {
    return orders.iterator();
  }

  public Sort on(String attr) {
    return on(attr, Direction.ASC);
  }

  public Sort on(String attr, Direction direction) {
    orders.add(new Order(attr, direction));
    return this;
  }

  @Override
  public String toString() {
    return "Sort [orders=" + orders + "]";
  }

  public static Sort parse(String orderByStr) {
    Sort sort = new Sort();
    for (String sortClauseStr : StringUtils.split(orderByStr, ';')) {
      String[] tokens = StringUtils.split(sortClauseStr, ',');
      if (tokens.length == 1) {
        sort.on(tokens[0]);
      } else {
        sort.on(tokens[0], Direction.valueOf(tokens[1]));
      }
    }
    return sort;
  }

  public String toSortString() {
    return orders.stream()
        .map(order -> order.getAttr() + ',' + order.getDirection().toString())
        .collect(Collectors.joining(";"));
  }

  public static class Order {
    private final String attr;
    private final Direction direction;

    public Order(String attr) {
      this(attr, Direction.ASC);
    }

    public Order(String attr, Direction direction) {
      this.attr = attr;
      this.direction = direction;
    }

    public String getAttr() {
      return attr;
    }

    public Direction getDirection() {
      return direction;
    }

    @Override
    public String toString() {
      return "Order [attr=" + attr + ", direction=" + direction + "]";
    }
  }

  public enum Direction {
    ASC,
    DESC
  }

  public boolean hasField(String attributeName) {
    for (Order order : this.orders) {
      if (order.getAttr().equals(attributeName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sort orders1 = (Sort) o;
    return Objects.equals(orders, orders1.orders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orders);
  }
}
