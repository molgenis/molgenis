package org.molgenis.metadata.manager.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_EditorEntityType.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class EditorEntityType {
  public abstract String getId();

  @CheckForNull
  public abstract String getLabel();

  public abstract Map<String, String> getLabelI18n();

  @CheckForNull
  public abstract String getDescription();

  public abstract Map<String, String> getDescriptionI18n();

  public abstract boolean isAbstract();

  public abstract String getBackend();

  @CheckForNull
  public abstract EditorPackageIdentifier getPackage();

  @CheckForNull
  public abstract EditorEntityTypeParent getEntityTypeParent();

  public abstract List<EditorAttribute> getAttributes();

  public abstract List<EditorAttributeIdentifier> getReferringAttributes();

  public abstract List<EditorTagIdentifier> getTags();

  @CheckForNull
  public abstract EditorAttributeIdentifier getIdAttribute();

  @CheckForNull
  public abstract EditorAttributeIdentifier getLabelAttribute();

  public abstract List<EditorAttributeIdentifier> getLookupAttributes();

  public static EditorEntityType create(
      String id,
      @CheckForNull String label,
      Map<String, String> i18nLabel,
      @CheckForNull String description,
      Map<String, String> i18nDescription,
      boolean isAbstract,
      String backend,
      @CheckForNull EditorPackageIdentifier aPackage,
      @CheckForNull EditorEntityTypeParent entityTypeParent,
      List<EditorAttribute> attributes,
      List<EditorAttributeIdentifier> referringAttributes,
      List<EditorTagIdentifier> tags,
      @CheckForNull EditorAttributeIdentifier idAttribute,
      @CheckForNull EditorAttributeIdentifier labelAttribute,
      List<EditorAttributeIdentifier> lookupAttributes) {
    return new AutoValue_EditorEntityType(
        id,
        label,
        i18nLabel,
        description,
        i18nDescription,
        isAbstract,
        backend,
        aPackage,
        entityTypeParent,
        attributes,
        referringAttributes,
        tags,
        idAttribute,
        labelAttribute,
        lookupAttributes);
  }
}
