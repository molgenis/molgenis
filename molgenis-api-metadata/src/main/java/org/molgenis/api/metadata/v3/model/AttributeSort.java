package org.molgenis.api.metadata.v3.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.metadata.v3.model.AttributeSort.Order.Direction;

@AutoValue
public abstract class AttributeSort {
  public static final AttributeSort EMPTY_SORT = AttributeSort.create(emptyList());

  public abstract List<Order> getOrders();

  public static AttributeSort create(String newItem) {
    return create(newItem, null);
  }

  public static AttributeSort create(String newItem, @Nullable @CheckForNull Direction direction) {
    return create(singletonList(Order.create(newItem, direction)));
  }

  public static AttributeSort create(List<Order> newOrders) {
    return builder().setOrders(newOrders).build();
  }

  public static Builder builder() {
    return new AutoValue_AttributeSort.Builder();
  }

  @AutoValue
  public abstract static class Order {
    public enum Direction {
      ASC,
      DESC
    }

    public abstract String getItem();

    @Nullable
    @CheckForNull
    public abstract Direction getDirection();

    public static Order create(String newItem) {
      return create(newItem, null);
    }

    public static Order create(String newItem, @Nullable @CheckForNull Direction newDirection) {
      return builder().setItem(newItem).setDirection(newDirection).build();
    }

    public static Builder builder() {
      return new AutoValue_AttributeSort_Order.Builder();
    }

    @SuppressWarnings(
        "squid:S1610") // Abstract classes without fields should be converted to interfaces
    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder setItem(String newItem);

      public abstract Builder setDirection(@Nullable @CheckForNull Direction newDirection);

      public abstract Order build();
    }
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setOrders(List<Order> newOrders);

    public abstract AttributeSort build();
  }
}
