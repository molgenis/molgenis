package org.molgenis.dataexplorer.controller;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NavigatorLink.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class NavigatorLink {
  public abstract String getHref();

  @Nullable
  @CheckForNull
  public abstract String getLabel();

  public static NavigatorLink create(String href, String label) {
    return new AutoValue_NavigatorLink(href, label);
  }
}
