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
  public abstract String getLogoTopHref();

  public abstract Integer getLogoTopMaxHeight();

  @Nullable
  @CheckForNull
  public abstract String getLogoNavBarHref();

  public abstract JsonObject getMenu();

  public abstract String getloginHref();

  public abstract String gethelpLink();

  public abstract Boolean getAuthenticated();

  public abstract Boolean getShowCookieWall();

  public static Builder builder() {
    return new AutoValue_UiContextResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLogoTopHref(String logoTopHref);

    public abstract Builder setLogoTopMaxHeight(Integer logoTopMaxHeight);

    public abstract Builder setLogoNavBarHref(String logoNavBarHref);

    public abstract Builder setLoginHref(String loginHref);

    public abstract Builder setHelpLink(String helpLink);

    public abstract Builder setMenu(JsonObject menu);

    public abstract Builder setAuthenticated(Boolean authenticated);

    public abstract Builder setShowCookieWall(Boolean showCookieWall);

    public abstract UiContextResponse build();
  }
}
