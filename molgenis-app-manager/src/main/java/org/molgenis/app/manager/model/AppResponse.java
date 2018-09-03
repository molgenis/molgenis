package org.molgenis.app.manager.model;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import org.molgenis.app.manager.meta.App;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AppResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class AppResponse {
  public abstract String getId();

  public abstract String getName();

  public abstract String getLabel();

  public abstract String getDescription();

  public abstract boolean getIsActive();

  public abstract boolean getIncludeMenuAndFooter();

  public abstract String getTemplateContent();

  public abstract String getVersion();

  public abstract String getResourceFolder();

  @Nullable
  public abstract String getAppConfig();

  public static AppResponse create(App app) {
    return new AutoValue_AppResponse(
        app.getId(),
        app.getName(),
        app.getLabel(),
        app.getDescription(),
        app.isActive(),
        app.includeMenuAndFooter(),
        app.getTemplateContent(),
        app.getAppVersion(),
        app.getResourceFolder(),
        app.getAppConfig());
  }
}
