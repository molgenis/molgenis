package org.molgenis.api.model;

import static java.util.Collections.singletonList;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Sort.Order.Direction;

@AutoValue
public abstract class Sort {
  public abstract List<Order> getOrders();

  public static Sort create(String newItem) {
    return create(newItem, null);
  }

  public static Sort create(String newItem, @Nullable @CheckForNull Direction newDirection) {
    return create(singletonList(Order.create(newItem, newDirection)));
  }

  public static Sort create(List<Order> newOrders) {
    return builder().setOrders(newOrders).build();
  }

  public static Builder builder() {
    return new AutoValue_Sort.Builder();
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
      return new AutoValue_Sort_Order.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder setItem(String newItem);

      public abstract Builder setDirection(@Nullable @CheckForNull Direction newDirection);

      public abstract Order build();
    }
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setOrders(List<Order> newOrders);

    public abstract Sort build();
  }
}
