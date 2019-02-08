package org.molgenis.core.ui.controller;

import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@AutoValue
@SuppressWarnings("squid:S1610")
public abstract class UiContextResponse {

  @Nullable
  @CheckForNull
  public abstract String getLogoTop();

  public abstract Integer getLogoTopMaxHeight();

  @Nullable
  @CheckForNull
  public abstract String getNavBarLogo();

  public abstract JsonObject getMenu();

  public abstract String getloginHref();

  public abstract String gethelpLink();

  public abstract Boolean getAuthenticated();

  public abstract Boolean getShowCookieWall();

  @Nullable
  @CheckForNull
  public abstract String getAdditionalMessage();

  public abstract String getVersion();

  public abstract String getBuildDate();

  public static Builder builder() {
    return new AutoValue_UiContextResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLogoTop(String logoTop);

    public abstract Builder setLogoTopMaxHeight(Integer logoTopMaxHeight);

    public abstract Builder setNavBarLogo(String logoNavBar);

    public abstract Builder setLoginHref(String loginHref);

    public abstract Builder setHelpLink(String helpLink);

    public abstract Builder setMenu(JsonObject menu);

    public abstract Builder setAuthenticated(Boolean authenticated);

    public abstract Builder setShowCookieWall(Boolean showCookieWall);

    public abstract Builder setAdditionalMessage(String additionalMessage);

    public abstract Builder setVersion(String version);

    public abstract Builder setBuildDate(String buildDate);

    public abstract UiContextResponse build();
  }
}
