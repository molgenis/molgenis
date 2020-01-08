/*
 * Copyright 2008-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.molgenis.api.data.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Adapted from org.springframework.data.domain.Sort for backwards compatibility
 *
 * <p>Sort option for queries. You have to provide at least a list of properties to sort for that
 * must not include {@literal null} or empty strings. The direction defaults to {@link
 * SortV1#DEFAULT_DIRECTION}.
 *
 * @author Oliver Gierke
 */
public class SortV1 implements Iterable<SortV1.OrderV1>, Serializable {

  private static final long serialVersionUID = 5737186511678863905L;
  public static final DirectionV1 DEFAULT_DIRECTION = DirectionV1.ASC;

  private final List<OrderV1> orders;

  /** Creates a new {@link SortV1} instance. */
  public SortV1(DirectionV1 direction, List<String> properties) {

    if (properties == null || properties.isEmpty()) {
      throw new IllegalArgumentException("You have to provide at least one property to sort by!");
    }

    this.orders = new ArrayList<>(properties.size());

    for (String property : properties) {
      this.orders.add(new OrderV1(direction, property));
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<OrderV1> iterator() {
    return this.orders.iterator();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof SortV1)) {
      return false;
    }

    SortV1 that = (SortV1) obj;

    return this.orders.equals(that.orders);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {

    int result = 17;
    result = 31 * result + orders.hashCode();
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return StringUtils.join(orders, ',');
  }

  /**
   * Enumeration for sort directions.
   *
   * @author Oliver Gierke
   */
  @SuppressWarnings("unused")
  public enum DirectionV1 {
    ASC,
    DESC
  }

  /**
   * PropertyPath implements the pairing of an {@link DirectionV1} and a property. It is used to
   * provide input for {@link SortV1}
   *
   * @author Oliver Gierke
   */
  public static class OrderV1 implements Serializable {

    private static final long serialVersionUID = 1522511010900108987L;

    private final DirectionV1 direction;
    private final String property;

    /**
     * Creates a new {@link OrderV1} instance. if order is {@literal null} then order defaults to
     * {@link SortV1#DEFAULT_DIRECTION}
     *
     * @param direction can be {@literal null}, will default to {@link SortV1#DEFAULT_DIRECTION}
     * @param property must not be {@literal null} or empty.
     */
    public OrderV1(DirectionV1 direction, String property) {

      if (!StringUtils.isNotEmpty(property)) {
        throw new IllegalArgumentException("Property must not null or empty!");
      }

      this.direction = direction == null ? DEFAULT_DIRECTION : direction;
      this.property = property;
    }

    /** @deprecated use {@link SortV1#SortV1(DirectionV1, List)} instead. */
    @Deprecated
    public static List<OrderV1> create(DirectionV1 direction, Iterable<String> properties) {

      List<OrderV1> orders = new ArrayList<>();
      for (String property : properties) {
        orders.add(new OrderV1(direction, property));
      }
      return orders;
    }

    /** Returns the order the property shall be sorted for. */
    public DirectionV1 getDirection() {
      return direction;
    }

    /** Returns the property to order for. */
    public String getProperty() {
      return property;
    }

    /** Returns whether sorting for this property shall be ascending. */
    public boolean isAscending() {
      return this.direction.equals(DirectionV1.ASC);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

      int result = 17;

      result = 31 * result + direction.hashCode();
      result = 31 * result + property.hashCode();

      return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

      if (this == obj) {
        return true;
      }

      if (!(obj instanceof OrderV1)) {
        return false;
      }

      OrderV1 that = (OrderV1) obj;

      return this.direction.equals(that.direction) && this.property.equals(that.property);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return String.format("%s: %s", property, direction);
    }
  }
}
