package org.molgenis.api.metadata.v3.job;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SerializableEntityType.class)
abstract class SerializableEntityType {
  abstract String getId();

  @Nullable
  @CheckForNull
  abstract String getPackageId();

  abstract String getLabel();

  abstract ImmutableMap<String, String> getLabelI18n();

  @Nullable
  @CheckForNull
  abstract String getDescription();

  abstract ImmutableMap<String, String> getDescriptionI18n();

  abstract ImmutableList<SerializableAttribute> getAttributes();

  abstract boolean isAbstract();

  @Nullable
  @CheckForNull
  abstract String getExtendsId();

  abstract ImmutableList<String> getTagIds();

  abstract String getBackend();

  abstract int getIndexingDepth();

  public static Builder builder() {
    return new AutoValue_SerializableEntityType.Builder();
  }

  @SuppressWarnings(
      "java:S1610") // Abstract classes without fields should be converted to interfaces
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String newId);

    public abstract Builder setPackageId(String newPackageId);

    public abstract Builder setLabel(String newLabel);

    public abstract Builder setLabelI18n(ImmutableMap<String, String> newLabelI18n);

    public abstract Builder setDescription(String newDescription);

    public abstract Builder setDescriptionI18n(ImmutableMap<String, String> newDescriptionI18n);

    public abstract Builder setAttributes(ImmutableList<SerializableAttribute> newAttributes);

    public abstract Builder setAbstract(boolean newAbstract);

    public abstract Builder setExtendsId(String newExtendsId);

    public abstract Builder setTagIds(ImmutableList<String> newTagIds);

    public abstract Builder setBackend(String newBackend);

    public abstract Builder setIndexingDepth(int newIndexingDepth);

    public abstract SerializableEntityType build();
  }
}
