package org.molgenis.api.meta.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_CreateEntityTypeRequest.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class CreateEntityTypeRequest {
  public abstract String getId();

  @Nullable
  @CheckForNull
  public abstract I18nValue getLabel();

  @Nullable
  @CheckForNull
  public abstract I18nValue getDescription();

  public abstract boolean isAbstract();

  public abstract String getBackend();

  @Nullable
  @CheckForNull
  public abstract String getPackage();

  @Nullable
  @CheckForNull
  public abstract String getEntityTypeParent();

  public abstract List<CreateAttributeRequest> getAttributes();

  @Nullable
  @CheckForNull
  public abstract String getIdAttribute();

  @Nullable
  @CheckForNull
  public abstract String getLabelAttribute();

  public abstract List<String> getLookupAttributes();

  public static CreateEntityTypeRequest create(
      String id,
      @Nullable @CheckForNull I18nValue label,
      @Nullable @CheckForNull I18nValue description,
      boolean isAbstract,
      String backend,
      @Nullable @CheckForNull String aPackage,
      @Nullable @CheckForNull String entityTypeParent,
      List<CreateAttributeRequest> attributes,
      @Nullable @CheckForNull String idAttribute,
      @Nullable @CheckForNull String labelAttribute,
      List<String> lookupAttributes) {
    return new AutoValue_CreateEntityTypeRequest(
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
