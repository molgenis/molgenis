package org.molgenis.api.metadata.v3.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public interface MetadataResponseData {
  String getId();

  @Nullable
  @CheckForNull
  String getLabel();

  @Nullable
  @CheckForNull
  I18nValue getLabelI18n();

  @Nullable
  @CheckForNull
  String getDescription();

  @Nullable
  @CheckForNull
  I18nValue getDescriptionI18n();
}
