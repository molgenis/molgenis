package org.molgenis.data.settings;

/**
 * Application settings: See {@link org.molgenis.ui.settings.AppDbSettings.Meta} for setting descriptions and allowed
 * return values.
 */
public interface AppSettings
{
	String getTitle();

	void setTitle(String title);

	String getLogoTopHref();

	void setLogoTopHref(String logoHref);

	String getLogoNavBarHref();

	void setLogoNavBarHref(String logoHref);

	String getFooter();

	void setFooter(String footerText);

	boolean getSignUp();

	void setSignUp(boolean signUp);

	boolean getSignUpModeration();

	void setSignUpModeration(boolean signUpModeration);

	String getLanguageCode();

	void setLanguageCode(String languageCode);

	String getBootstrapTheme();

	void setBootstrapTheme(String bootstrapTheme);

	String getCssHref();

	void setCssHref(String cssHref);

	String getMenu();

	void setMenu(String menuJson);

	Integer getAggregateThreshold();

	void setAggregateThreshold(Integer threshold);

	String getTrackingCodeFooter();

	void setTrackingCodeFooter(String trackingCodeFooter);

	boolean getGoogleAnalyticsIpAnonymization();

	void setGoogleAnalyticsIpAnonymization(boolean googleAnalyticsPrivacyFriendlyTracking);

	String getGoogleAnalyticsTrackingId();

	void setGoogleAnalyticsTrackingId(String googleAnalyticsTrackingId);

	String getGoogleAnalyticsTrackingIdMolgenis();

	void setGoogleAnalyticsTrackingIdMolgenis(String googleAnalyticsTrackingIdMolgenis);

	boolean getGoogleAnalyticsAccountPrivacyFriendly();

	void setGoogleAnalyticsAccountPrivacyFriendly(boolean googleAnalyticsAccountPrivacyFriendly);

	boolean getGoogleAnalyticsAccountPrivacyFriendlyMolgenis();

}