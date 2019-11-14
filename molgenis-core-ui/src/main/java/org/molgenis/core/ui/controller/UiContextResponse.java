package org.molgenis.core.ui.controller;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.web.menu.model.Menu;

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

  @Nullable
  @CheckForNull
  public abstract Menu getMenu();

  public abstract String getloginHref();

  public abstract Map<String, String> gethelpLink();

  public abstract Boolean getAuthenticated();

  @Nullable
  @CheckForNull
  public abstract String getEmail();

  public abstract String getUsername();

  public abstract List<String> getRoles();

  public abstract Boolean getShowCookieWall();

  @Nullable
  @CheckForNull
  public abstract String getAdditionalMessage();

  public abstract String getVersion();

  public abstract String getBuildDate();

  @Nullable
  @CheckForNull
  public abstract String getCssHref();

  public static Builder builder() {
    return new AutoValue_UiContextResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLogoTop(String logoTop);

    public abstract Builder setLogoTopMaxHeight(Integer logoTopMaxHeight);

    public abstract Builder setNavBarLogo(String logoNavBar);

    public abstract Builder setLoginHref(String loginHref);

    public abstract Builder setHelpLink(Map<String, String> helpLink);

    public abstract Builder setMenu(Menu menu);

    public abstract Builder setAuthenticated(Boolean authenticated);

    public abstract Builder setEmail(String email);

    public abstract Builder setUsername(String username);

    public abstract Builder setRoles(List<String> roles);

    public abstract Builder setShowCookieWall(Boolean showCookieWall);

    public abstract Builder setAdditionalMessage(String additionalMessage);

    public abstract Builder setVersion(String version);

    public abstract Builder setBuildDate(String buildDate);

    public abstract Builder setCssHref(String cssHref);

    public abstract UiContextResponse build();
  }
}
