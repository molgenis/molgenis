package org.molgenis.integrationtest.data;

import org.molgenis.settings.AppSettings;

public class TestAppSettings implements AppSettings {
  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public void setTitle(String title) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLogoTopHref() {
    return null;
  }

  @Override
  public void setLogoTopHref(String logoHref) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLogoTopMaxHeight() {
    return 0;
  }

  @Override
  public void setLogoTopMaxHeight(int fixedHeight) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLogoNavBarHref() {
    return null;
  }

  @Override
  public void setLogoNavBarHref(String logoHref) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getFooter() {
    return null;
  }

  @Override
  public void setFooter(String footerText) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getLanguageCode() {
    return null;
  }

  @Override
  public void setLanguageCode(String languageCode) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBootstrapTheme() {
    return null;
  }

  @Override
  public void setBootstrapTheme(String bootstrapTheme) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCssHref() {
    return null;
  }

  @Override
  public void setCssHref(String cssHref) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getMenu() {
    return null;
  }

  @Override
  public void setMenu(String menuJson) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer getAggregateThreshold() {
    return null;
  }

  @Override
  public void setAggregateThreshold(Integer threshold) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getTrackingCodeFooter() {
    return null;
  }

  @Override
  public void setTrackingCodeFooter(String trackingCodeFooter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getGoogleAnalyticsIpAnonymization() {
    return false;
  }

  @Override
  public void setGoogleAnalyticsIpAnonymization(boolean googleAnalyticsPrivacyFriendlyTracking) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getGoogleAnalyticsTrackingId() {
    return null;
  }

  @Override
  public void setGoogleAnalyticsTrackingId(String googleAnalyticsTrackingId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getGoogleAnalyticsTrackingIdMolgenis() {
    return null;
  }

  @Override
  public void setGoogleAnalyticsTrackingIdMolgenis(String googleAnalyticsTrackingIdMolgenis) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getGoogleAnalyticsAccountPrivacyFriendly() {
    return false;
  }

  @Override
  public void setGoogleAnalyticsAccountPrivacyFriendly(
      boolean googleAnalyticsAccountPrivacyFriendly) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getGoogleAnalyticsAccountPrivacyFriendlyMolgenis() {
    return false;
  }

  @Override
  public void setCustomJavascript(String customJavascript) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCustomJavascript() {
    return null;
  }

  @Override
  public String getRecaptchaPrivateKey() {
    return null;
  }

  @Override
  public String getRecaptchaPublicKey() {
    return null;
  }

  @Override
  public boolean getRecaptchaIsEnabled() {
    return false;
  }

  @Override
  public double getRecaptchaBotThreshold() {
    return 0.0;
  }

  @Override
  public String getRecaptchaVerifyURI() {
    return null;
  }
}
