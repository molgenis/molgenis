package org.molgenis.core.ui.controller;

import com.google.auto.value.AutoValue;
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

  public static Builder builder() {
    return new AutoValue_UiContextResponse.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setLogoTopHref(String logoTopHref);

    public abstract Builder setLogoTopMaxHeight(Integer logoTopMaxHeight);

    public abstract Builder setLogoNavBarHref(String logoNavBarHref);

    public abstract UiContextResponse build();
  }
}
