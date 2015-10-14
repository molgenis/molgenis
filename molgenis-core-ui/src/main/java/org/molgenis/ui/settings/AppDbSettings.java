package org.molgenis.ui.settings;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Application settings that are read from a data source and persisted to a data source.
 */
@Component
public class AppDbSettings extends DefaultSettingsEntity implements AppSettings
{
	private static final long serialVersionUID = 1L;

	private static final String ID = "app";

	public AppDbSettings()
	{
		super(ID);
	}

	@Component
	public static class Meta extends DefaultSettingsEntityMetaData
	{
		private static final String TITLE = "title";
		private static final String LOGO_NAVBAR_HREF = "logo_href_navbar";
		private static final String LOGO_TOP_HREF = "logo_href_top";
		private static final String FOOTER = "footer";
		private static final String SIGNUP = "signup";
		private static final String SIGNUP_MODERATION = "signup_moderation";
		public static final String MENU = "molgenis_menu";
		private static final String LANGUAGE_CODE = "language_code";
		private static final String BOOTSTRAP_THEME = "bootstrap_theme";
		private static final String CSS_HREF = "css_href";

		private static final String TRACKING = "tracking";
		private static final String TRACKING_CODE_FOOTER = "tracking_code_footer";
		private static final String GOOGLE_ANALYTICS_IP_ANONYMIZATION = "ga_privacy_friendly";
		private static final String GOOGLE_ANALYTICS_TRACKING_ID = "ga_tracking_id";
		private static final String GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS = "ga_acc_privacy_friendly";
		private static final String GOOGLE_ANALYTICS_TRACKING_ID_MOLGENIS = "ga_tracking_id_mgs";
		private static final String GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS_MOLGENIS = "ga_acc_privacy_friendly_mgs";
		private static final String AGGREGATE_THRESHOLD = "aggregate_threshold";

		private static final String DEFAULT_TITLE = "MOLGENIS";
		private static final String DEFAULT_LOGO_NAVBAR_HREF = "/img/logo_molgenis_small.png";
		private static final boolean DEFAULT_SIGNUP = false;
		private static final boolean DEFAULT_SIGNUP_MODERATION = true;
		private static final String DEFAULT_LANGUAGE_CODE = "en";
		private static final String DEFAULT_BOOTSTRAP_THEME = "bootstrap-molgenis.min.css";
		private static final boolean DEFAULT_GOOGLE_ANALYTICS_IP_ANONYMIZATION = true;
		private static final boolean DEFAULT_GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS = false;
		private static final boolean DEFAULT_GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS_MOLGENIS = true;

		public Meta()
		{
			super(ID);
			setLabel("Application settings");
			setDescription("General application settings.");

			addAttribute(TITLE).setDataType(STRING).setNillable(false).setDefaultValue(DEFAULT_TITLE)
					.setLabel("Application title").setDescription("Displayed in browser toolbar.");
			addAttribute(SIGNUP).setDataType(BOOL).setNillable(false).setDefaultValue(String.valueOf(DEFAULT_SIGNUP))
					.setLabel("Allow users to sign up");
			addAttribute(SIGNUP_MODERATION).setDataType(BOOL).setNillable(false)
					.setDefaultValue(String.valueOf(DEFAULT_SIGNUP_MODERATION)).setLabel("Sign up moderation")
					.setDescription("Admins must accept sign up requests before account activation");
			addAttribute(LOGO_NAVBAR_HREF).setDataType(STRING).setNillable(true).setLabel("Logo in navigation bar")
					.setDefaultValue(DEFAULT_LOGO_NAVBAR_HREF)
					.setDescription("HREF to logo image used instead of home plugin label");
			addAttribute(LOGO_TOP_HREF).setDataType(STRING).setNillable(true).setLabel("Logo above navigation bar")
					.setDescription("HREF to logo image");
			addAttribute(FOOTER).setDataType(TEXT).setNillable(true).setLabel("Footer text");
			addAttribute(MENU).setDataType(TEXT).setNillable(true).setLabel("Menu")
					.setDescription("JSON object that describes menu content.");
			addAttribute(LANGUAGE_CODE).setDataType(STRING).setNillable(false).setDefaultValue(DEFAULT_LANGUAGE_CODE)
					.setLabel("Language code").setDescription("ISO 639 alpha-2 or alpha-3 language code.");
			addAttribute(BOOTSTRAP_THEME).setDataType(STRING).setNillable(false)
					.setDefaultValue(DEFAULT_BOOTSTRAP_THEME).setLabel("Bootstrap theme")
					.setDescription("CSS file name of theme (see molgenis-core-ui/src/main/resources/css/themes).");
			addAttribute(CSS_HREF).setDataType(STRING).setNillable(true).setLabel("CSS href")
					.setDescription("CSS file name to add custom CSS (see molgenis-core-ui/src/main/resources/css).");

			addAttribute(AGGREGATE_THRESHOLD).setDataType(INT).setNillable(true).setLabel("Aggregate threshold")
					.setDescription(
							"Aggregate value counts below this threshold are reported as the threshold. (e.g. a count of 100 is reported as <= 10)");

			// tracking settings
			DefaultAttributeMetaData trackingAttr = addAttribute(TRACKING).setDataType(COMPOUND).setLabel("Tracking");

			DefaultAttributeMetaData gaTrackingPrivacyFriendlyAttr = new DefaultAttributeMetaData(
					GOOGLE_ANALYTICS_IP_ANONYMIZATION).setDataType(BOOL).setNillable(false)
							.setDefaultValue(String.valueOf(DEFAULT_GOOGLE_ANALYTICS_IP_ANONYMIZATION))
							.setLabel("IP anonymization").setDescription(
									"Disables the cookie wall by using privacy friendly tracking (only works if google analytics accounts are configured correctly, see below)");
			DefaultAttributeMetaData gaTrackingIdAttr = new DefaultAttributeMetaData(GOOGLE_ANALYTICS_TRACKING_ID)
					.setDataType(STRING).setNillable(true).setLabel("Google analytics tracking ID")
					.setDescription("Google analytics tracking ID (e.g. UA-XXXX-Y)");
			DefaultAttributeMetaData gaAccountPrivacyFriendlyAttr = new DefaultAttributeMetaData(
					GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS).setDataType(BOOL).setNillable(false)
							.setDefaultValue(String.valueOf(DEFAULT_GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS))
							.setLabel("Google analytics account privacy friendly").setDescription(
									"Confirm that you have configured your Google Analytics account as described here: https://cbpweb.nl/sites/default/files/atoms/files/handleiding_privacyvriendelijk_instellen_google_analytics_0.pdf");
			DefaultAttributeMetaData gaTrackingIdMolgenisAttr = new DefaultAttributeMetaData(
					GOOGLE_ANALYTICS_TRACKING_ID_MOLGENIS).setDataType(STRING).setNillable(true)
							.setLabel("Google analytics tracking ID (MOLGENIS)")
							.setDescription("Google analytics tracking ID used by MOLGENIS");
			DefaultAttributeMetaData gaAccountPrivacyFriendlyMolgenisAttr = new DefaultAttributeMetaData(
					GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS_MOLGENIS).setDataType(BOOL)
							.setNillable(false)
							.setDefaultValue(
									String.valueOf(DEFAULT_GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS_MOLGENIS))
							.setReadOnly(true).setLabel("Google analytics account privacy friendly (MOLGENIS)")
							.setDescription(
									"Confirm that the MOLGENIS Google Analytics account is configured as described here: https://cbpweb.nl/sites/default/files/atoms/files/handleiding_privacyvriendelijk_instellen_google_analytics_0.pdf");
			DefaultAttributeMetaData trackingFooterAttr = new DefaultAttributeMetaData(TRACKING_CODE_FOOTER)
					.setDataType(SCRIPT).setNillable(true).setLabel("Tracking code footer").setDescription(
							"JS tracking code that is placed in the footer HTML (e.g. PiWik). This enables the cookie wall.");

			trackingAttr.addAttributePart(gaTrackingPrivacyFriendlyAttr);
			trackingAttr.addAttributePart(gaTrackingIdAttr);
			trackingAttr.addAttributePart(gaAccountPrivacyFriendlyAttr);
			trackingAttr.addAttributePart(gaTrackingIdMolgenisAttr);
			trackingAttr.addAttributePart(gaAccountPrivacyFriendlyMolgenisAttr);
			trackingAttr.addAttributePart(trackingFooterAttr);
		}
	}

	@Override
	public String getTitle()
	{
		return getString(Meta.TITLE);
	}

	@Override
	public void setTitle(String title)
	{
		set(Meta.TITLE, title);
	}

	@Override
	public String getLogoTopHref()
	{
		return getString(Meta.LOGO_TOP_HREF);
	}

	@Override
	public void setLogoTopHref(String logoHref)
	{
		set(Meta.LOGO_TOP_HREF, logoHref);
	}

	@Override
	public String getLogoNavBarHref()
	{
		return getString(Meta.LOGO_NAVBAR_HREF);
	}

	@Override
	public void setLogoNavBarHref(String logoHref)
	{
		set(Meta.LOGO_NAVBAR_HREF, logoHref);
	}

	@Override
	public String getFooter()
	{
		return getString(Meta.FOOTER);
	}

	@Override
	public void setFooter(String footerText)
	{
		set(Meta.FOOTER, footerText);
	}

	@Override
	public boolean getSignUp()
	{
		Boolean value = getBoolean(Meta.SIGNUP);
		return value != null ? value.booleanValue() : false;
	}

	@Override
	public void setSignUp(boolean signUp)
	{
		set(Meta.SIGNUP, signUp);
	}

	@Override
	public boolean getSignUpModeration()
	{
		Boolean value = getBoolean(Meta.SIGNUP_MODERATION);
		return value != null ? value.booleanValue() : false;
	}

	@Override
	public void setSignUpModeration(boolean signUpModeration)
	{
		set(Meta.SIGNUP_MODERATION, signUpModeration);
	}

	@Override
	public String getLanguageCode()
	{
		return getString(Meta.LANGUAGE_CODE);
	}

	@Override
	public void setLanguageCode(String languageCode)
	{
		set(Meta.LANGUAGE_CODE, languageCode);
	}

	@Override
	public String getBootstrapTheme()
	{
		return getString(Meta.BOOTSTRAP_THEME);
	}

	@Override
	public void setBootstrapTheme(String bootstrapTheme)
	{
		// verify that css file exists
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource resource = resolver.getResource("/css/themes/" + bootstrapTheme);
		if (!resource.exists())
		{
			throw new MolgenisDataException("Bootstrap theme does not exist [/css/themes/" + bootstrapTheme + "]");
		}

		set(Meta.BOOTSTRAP_THEME, bootstrapTheme);
	}

	@Override
	public String getCssHref()
	{
		return getString(Meta.CSS_HREF);
	}

	@Override
	public void setCssHref(String cssHref)
	{
		set(Meta.CSS_HREF, cssHref);
	}

	@Override
	public String getMenu()
	{
		return getString(Meta.MENU);
	}

	@Override
	public void setMenu(String menuJson)
	{
		set(Meta.MENU, menuJson);
	}

	@Override
	public String getTrackingCodeFooter()
	{
		return getString(Meta.TRACKING_CODE_FOOTER);
	}

	@Override
	public void setTrackingCodeFooter(String trackingCodeHeader)
	{
		set(Meta.TRACKING_CODE_FOOTER, trackingCodeHeader);
	}

	@Override
	public String getGoogleAnalyticsTrackingId()
	{
		return getString(Meta.GOOGLE_ANALYTICS_TRACKING_ID);
	}

	@Override
	public void setGoogleAnalyticsTrackingId(String trackingCodeFooter)
	{
		set(Meta.GOOGLE_ANALYTICS_TRACKING_ID, trackingCodeFooter);
	}

	@Override
	public String getGoogleAnalyticsTrackingIdMolgenis()
	{
		return getString(Meta.GOOGLE_ANALYTICS_TRACKING_ID_MOLGENIS);
	}

	@Override
	public void setGoogleAnalyticsTrackingIdMolgenis(String googleAnalyticsTrackingIdMolgenis)
	{
		set(Meta.GOOGLE_ANALYTICS_TRACKING_ID_MOLGENIS, googleAnalyticsTrackingIdMolgenis);
	}

	@Override
	public Integer getAggregateThreshold()
	{
		return getInt(Meta.AGGREGATE_THRESHOLD);
	}

	@Override
	public void setAggregateThreshold(Integer threshold)
	{
		set(Meta.AGGREGATE_THRESHOLD, threshold);
	}

	@Override
	public boolean getGoogleAnalyticsIpAnonymization()
	{
		Boolean value = getBoolean(Meta.GOOGLE_ANALYTICS_IP_ANONYMIZATION);
		return value != null ? value.booleanValue() : false;
	}

	@Override
	public void setGoogleAnalyticsIpAnonymization(boolean googleAnalyticsIpAnonymization)
	{
		set(Meta.GOOGLE_ANALYTICS_IP_ANONYMIZATION, googleAnalyticsIpAnonymization);
	}

	@Override
	public boolean getGoogleAnalyticsAccountPrivacyFriendly()
	{
		Boolean value = getBoolean(Meta.GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS);
		return value != null ? value.booleanValue() : false;
	}

	@Override
	public void setGoogleAnalyticsAccountPrivacyFriendly(boolean googleAnalyticsAccountPrivacyFriendly)
	{
		set(Meta.GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS, googleAnalyticsAccountPrivacyFriendly);
	}

	@Override
	public boolean getGoogleAnalyticsAccountPrivacyFriendlyMolgenis()
	{
		Boolean value = getBoolean(Meta.GOOGLE_ANALYTICS_ACCOUNT_PRIVACY_FRIENDLY_SETTINGS_MOLGENIS);
		return value != null ? value.booleanValue() : false;
	}
}
