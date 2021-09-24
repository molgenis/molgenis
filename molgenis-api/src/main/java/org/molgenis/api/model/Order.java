package org.molgenis.api.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Order.class)
public abstract class Order {
  public enum Direction {
    ASC,
    DESC
  }

  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract Direction getDirection();

  public static Order create(String newItem) {
    return create(newItem, null);
  }

  public static Order create(String newItem, @Nullable @CheckForNull Direction newDirection) {
    return builder().setId(newItem).setDirection(newDirection).build();
  }

  public static Builder builder() {
    return new AutoValue_Order.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setDirection(@Nullable @CheckForNull Direction newDirection);

    public abstract Order build();
  }
}
