package org.molgenis.data.elasticsearch.generator.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@AutoValue
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class FieldMapping {
  public abstract String getName();

  public abstract MappingType getType();

  /**
   * For FieldMappings of type {@link MappingType#KEYWORD}, indicates if the keyword is case
   * sensitive.
   */
  public abstract boolean isCaseSensitive();

  /** Do want to index as NGRAM as well? */
  public abstract boolean isNeedsNgram();

  @Nullable
  @CheckForNull
  public abstract List<FieldMapping> getNestedFieldMappings();

  public static FieldMapping create(
      String newName, MappingType newType, List<FieldMapping> newNestedFieldMappings) {
    return builder()
        .setName(newName)
        .setType(newType)
        .setNestedFieldMappings(newNestedFieldMappings)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_FieldMapping.Builder().setCaseSensitive(false).setNeedsNgram(false);
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String newName);

    public abstract Builder setType(MappingType newType);

    public abstract Builder setCaseSensitive(boolean caseSensitive);

    public abstract Builder setNeedsNgram(boolean needsNgram);

    public abstract Builder setNestedFieldMappings(List<FieldMapping> newNestedFieldMappings);

    public abstract FieldMapping build();
  }
}
