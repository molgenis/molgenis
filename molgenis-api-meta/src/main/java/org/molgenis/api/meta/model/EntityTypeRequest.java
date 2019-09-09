package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EntityTypeRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EntityTypeRequest {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract I18nResponse getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nResponse getDescription();

  public abstract boolean isAbstract();

  public abstract String getBackend();

  @Nullable
  @CheckForNull
  public abstract String getPackage();

  @Nullable
  @CheckForNull
  public abstract String getEntityTypeParent();

  public abstract List<AttributeRequest> getAttributes();

  @Nullable
  @CheckForNull
  public abstract String getIdAttribute();

  @Nullable
  @CheckForNull
  public abstract String getLabelAttribute();

  public abstract List<String> getLookupAttributes();

  public static EntityTypeRequest create(
      String id,
      @Nullable @CheckForNull I18nResponse label,
      @Nullable @CheckForNull I18nResponse description,
      boolean isAbstract,
      String backend,
      @Nullable @CheckForNull String aPackage,
      @Nullable @CheckForNull String entityTypeParent,
      List<AttributeRequest> attributes,
      @Nullable @CheckForNull String idAttribute,
      @Nullable @CheckForNull String labelAttribute,
      List<String> lookupAttributes) {
    return new AutoValue_EntityTypeRequest(
        id,
        label,
        description,
        isAbstract,
        backend,
        aPackage,
        entityTypeParent,
        attributes,
        idAttribute,
        labelAttribute,
        lookupAttributes);
  }
}
