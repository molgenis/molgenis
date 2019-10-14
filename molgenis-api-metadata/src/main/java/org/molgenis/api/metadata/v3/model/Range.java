package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Range.class)
public abstract class Range {
  public abstract long getMin();

  public abstract long getMax();

  public static Range create(long min, long max) {
    return builder().setMin(min).setMax(max).build();
  }

  public static Range.Builder builder() {
    return new AutoValue_Range.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Range.Builder setMin(long min);

    public abstract Range.Builder setMax(long max);

    public abstract Range build();
  }
}
