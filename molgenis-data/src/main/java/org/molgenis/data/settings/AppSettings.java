package org.molgenis.data.settings;

/**
 * Application settings: See {@link org.molgenis.ui.settings.AppDbSettings.Meta} for setting descriptions and allowed
 * return values.
 */
public interface AppSettings
{
	String getTitle();

	String getLogoTopHref();

	String getLogoNavBarHref();

	void setLogoNavBarHref(String logoHref);

	String getFooter();

	boolean getSignUp();

	boolean getSignUpModeration();

	String getLanguageCode();

	String getBootstrapTheme();

	void setBootstrapTheme(String bootstrapTheme);

	String getCssHref();

	String getJsHref();

	String getMenu();

	void setMenu(String menuJson);

	String getTrackingCodeHeader();

	String getTrackingCodeFooter();

	Integer getAggregateThreshold();
}