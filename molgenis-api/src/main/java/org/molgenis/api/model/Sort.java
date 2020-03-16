package org.molgenis.api.model;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.api.model.Order.Direction;

@AutoValue
public abstract class Sort {
  public static final Sort EMPTY_SORT = Sort.create(emptyList());

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

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setOrders(List<Order> newOrders);

    public abstract Sort build();
  }
}
