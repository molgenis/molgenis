package org.molgenis.api.metadata.v3.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Range.class)
public abstract class Range {
  @Nullable
  @CheckForNull
  public abstract Long getMin();

  @Nullable
  @CheckForNull
  public abstract Long getMax();

  public static Range create(Long min, Long max) {
    return builder().setMin(min).setMax(max).build();
  }

  public static Range.Builder builder() {
    return new AutoValue_Range.Builder();
  }

  @SuppressWarnings(
      "squid:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Range.Builder setMin(Long min);

    public abstract Range.Builder setMax(Long max);

    public abstract Range build();
  }
}
