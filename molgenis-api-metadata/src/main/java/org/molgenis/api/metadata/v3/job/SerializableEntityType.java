package org.molgenis.api.metadata.v3.job;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
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

  public static SerializableEntityType create(
      String newId,
      String newPackageId,
      String newLabel,
      Map<String, String> newLabelI18n,
      String newDescription,
      Map<String, String> newDescriptionI18n,
      List<SerializableAttribute> newAttributes,
      boolean newAbstract,
      String newExtendsId,
      List<String> newTagIds,
      String newBackend,
      int newIndexingDepth) {
    return builder()
        .setId(newId)
        .setPackageId(newPackageId)
        .setLabel(newLabel)
        .setLabelI18n(ImmutableMap.copyOf(newLabelI18n))
        .setDescription(newDescription)
        .setDescriptionI18n(ImmutableMap.copyOf(newDescriptionI18n))
        .setAttributes(ImmutableList.copyOf(newAttributes))
        .setAbstract(newAbstract)
        .setExtendsId(newExtendsId)
        .setTagIds(ImmutableList.copyOf(newTagIds))
        .setBackend(newBackend)
        .setIndexingDepth(newIndexingDepth)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_SerializableEntityType.Builder();
  }

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
