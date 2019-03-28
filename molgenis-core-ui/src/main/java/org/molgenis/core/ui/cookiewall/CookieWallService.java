package org.molgenis.core.ui.cookiewall;

import static java.util.Objects.requireNonNull;

import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Component;

@Component
public class CookieWallService {

  private final AppSettings appSettings;

  public CookieWallService(AppSettings appSettings) {
    this.appSettings = requireNonNull(appSettings);
  }

  public boolean showCookieWall() {
    return !appSettings.getGoogleAnalyticsIpAnonymization() && showWallForUserDefinedTracking()
        || showWallForMolgenisTracking();
  }

  private boolean showWallForUserDefinedTracking() {
    return isSet(appSettings.getGoogleAnalyticsTrackingId())
        && !appSettings.getGoogleAnalyticsAccountPrivacyFriendly();
  }

  private boolean showWallForMolgenisTracking() {
    return isSet(appSettings.getGoogleAnalyticsTrackingIdMolgenis())
        && !appSettings.getGoogleAnalyticsAccountPrivacyFriendlyMolgenis();
  }

  private boolean isSet(String val) {
    return val != null && !val.isEmpty();
  }
}
