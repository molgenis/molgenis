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

	String getTrackingCodeHeader();

	void setTrackingCodeHeader(String trackingCodeHeader);

	String getTrackingCodeFooter();

	void setTrackingCodeFooter(String trackingCodeFooter);

	Integer getAggregateThreshold();

	void setAggregateThreshold(Integer threshold);
}