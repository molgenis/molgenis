package org.molgenis.api.meta;

import com.google.auto.value.AutoValue;
import java.util.List;
import org.molgenis.data.meta.model.Attribute;

@AutoValue
abstract class Attributes {
  abstract List<Attribute> getAttributes();

  abstract int getTotal();

  public static Attributes create(List<Attribute> attributes, int newTotal) {
    return builder().setAttributes(attributes).setTotal(newTotal).build();
  }

  public static Builder builder() {
    return new AutoValue_Attributes.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setAttributes(List<Attribute> attributes);

    public abstract Builder setTotal(int newTotal);

    public abstract Attributes build();
  }
}
