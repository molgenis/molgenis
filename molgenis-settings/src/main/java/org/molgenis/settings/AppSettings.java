package org.molgenis.settings;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/** Application settings */
public interface AppSettings {
  /** @return application title */
  String getTitle();

  /** @param title application title */
  void setTitle(String title);

  /** @return href of logo displayed above top menu */
  @Nullable
  @CheckForNull
  String getLogoTopHref();

  /** @param logoHref href of logo displayed above top menu */
  void setLogoTopHref(String logoHref);

  /** @return integer top logo uses int value as a max-height in px */
  int getLogoTopMaxHeight();

  /** @param fixedHeight value for top logo max-height in px */
  void setLogoTopMaxHeight(int fixedHeight);

  /** @return href of logo displayed in menu */
  @Nullable
  @CheckForNull
  String getLogoNavBarHref();

  /** @param logoHref href of logo displayed in menu */
  void setLogoNavBarHref(String logoHref);

  /** @return footer text */
  @Nullable
  @CheckForNull
  String getFooter();

  /** @param footerText footer text */
  void setFooter(String footerText);

  /** @return application ISO 639 alpha-2 or alpha-3 language code */
  String getLanguageCode();

  /** @param languageCode application ISO 639 alpha-2 or alpha-3 language code */
  void setLanguageCode(String languageCode);

  /** @return application bootstrap theme file name without path */
  String getBootstrapTheme();

  /** @param bootstrapTheme application bootstrap theme file name without path */
  void setBootstrapTheme(String bootstrapTheme);

  /** @return file name containing custom CSS without path */
  @Nullable
  @CheckForNull
  String getCssHref();

  /** @param cssHref file name containing custom CSS without path */
  void setCssHref(String cssHref);

  /** @return JSON object string representation of menu */
  @Nullable
  @CheckForNull
  String getMenu();

  /** @param menuJson JSON object string representation of menu */
  void setMenu(String menuJson);

  /** @return data aggregation threshold or <code>null</code> if no threshold exists */
  @Nullable
  @CheckForNull
  Integer getAggregateThreshold();

  /** @param threshold data aggregation threshold, <code>null</code> implies no threshold */
  void setAggregateThreshold(Integer threshold);

  /** @return JS string containing tracking code to be placed in the footer */
  @Nullable
  @CheckForNull
  String getTrackingCodeFooter();

  /** @param trackingCodeFooter JS string containing tracking code to be placed in the footer */
  void setTrackingCodeFooter(String trackingCodeFooter);

  /** @return whether Google analytics data is collected anonymously */
  boolean getGoogleAnalyticsIpAnonymization();

  /**
   * @param googleAnalyticsPrivacyFriendlyTracking <code>true</code> if Google analytics data is
   *     collected anonymously
   */
  void setGoogleAnalyticsIpAnonymization(boolean googleAnalyticsPrivacyFriendlyTracking);

  /** @return 3rd party Google analytics tracking ID */
  @Nullable
  @CheckForNull
  String getGoogleAnalyticsTrackingId();

  /** @param googleAnalyticsTrackingId 3rd party Google analytics tracking ID */
  void setGoogleAnalyticsTrackingId(String googleAnalyticsTrackingId);

  /** @return MOLGENIS Google analytics tracking ID */
  @Nullable
  @CheckForNull
  String getGoogleAnalyticsTrackingIdMolgenis();

  /** @param googleAnalyticsTrackingIdMolgenis MOLGENIS Google analytics tracking ID */
  void setGoogleAnalyticsTrackingIdMolgenis(String googleAnalyticsTrackingIdMolgenis);

  /**
   * Privacy friendly: as described here:
   * https://cbpweb.nl/sites/default/files/atoms/files/handleiding_privacyvriendelijk_instellen_google_analytics_0.pdf
   *
   * @return whether the 3rd Google Analytics account is configured to be privacy friendly
   */
  boolean getGoogleAnalyticsAccountPrivacyFriendly();

  /**
   * Privacy friendly: as described here:
   * https://cbpweb.nl/sites/default/files/atoms/files/handleiding_privacyvriendelijk_instellen_google_analytics_0.pdf
   *
   * @param googleAnalyticsAccountPrivacyFriendly whether the 3rd Google Analytics account is
   *     configured to be privacy friendly
   */
  void setGoogleAnalyticsAccountPrivacyFriendly(boolean googleAnalyticsAccountPrivacyFriendly);

  /**
   * Privacy friendly: as described here:
   * https://cbpweb.nl/sites/default/files/atoms/files/handleiding_privacyvriendelijk_instellen_google_analytics_0.pdf
   *
   * @return whether the MOLGENIS Google Analytics account is configured to be privacy friendly
   */
  boolean getGoogleAnalyticsAccountPrivacyFriendlyMolgenis();

  /**
   * @param customJavascript Custom JavaScript <script src="..."></script> headers, specified as
   *     comma separated list
   */
  void setCustomJavascript(String customJavascript);

  /**
   * The threshold that used to compare with the bot-score calculated by recaptcha
   *
   * @return threshold to determine if a bot is at work
   */
  double getRecaptchaBotThreshold();

  /**
   * Verification url used to access reCaptcha at google for BOT determination
   *
   * @return verification url
   */
  String getRecaptchaVerifyURI();

  /**
   * Private key or "secret" that is used serverside to verify the host
   *
   * @return secret
   */
  String getRecaptchaPrivateKey();

  /**
   * Public key or "site"-key that is used in the frontend to send to reCaptcha initially
   *
   * @return site key
   */
  String getRecaptchaPublicKey();

  /**
   * Is reCaptcha enabled
   *
   * @return is enabled
   */
  boolean getRecaptchaIsEnabled();

  /**
   * @return Custom JavaScript <script src="..."></script> headers, specified as comma separated
   *     list
   */
  @Nullable
  @CheckForNull
  String getCustomJavascript();
}
