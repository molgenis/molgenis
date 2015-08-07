package org.molgenis.ui.settings;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.settings.AppSettings;
import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.molgenis.data.support.MapEntity;
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

	@Override
	public String getTitle()
	{
		return getString(Meta.TITLE);
	}

	@Override
	public String getLogoTopHref()
	{
		return getString(Meta.LOGO_TOP_HREF);
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
	public boolean getSignUp()
	{
		Boolean value = getBoolean(Meta.SIGNUP);
		return value != null ? value.booleanValue() : false;
	}

	@Override
	public boolean getSignUpModeration()
	{
		Boolean value = getBoolean(Meta.SIGNUP_MODERATION);
		return value != null ? value.booleanValue() : false;
	}

	@Override
	public String getLanguageCode()
	{
		return getString(Meta.LANGUAGE_CODE);
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
	public String getJsHref()
	{
		return getString(Meta.JS_HREF);
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

	private static String TRACKING_CODE_PREFIX = "(function(){if('true' === $.cookie('permissionforcookies')){";
	private static String TRACKING_CODE_POSTFIX = "}})();";

	@Override
	public String getTrackingCodeHeader()
	{
		return getTrackingCode(Meta.TRACKING_CODE_HEADER);
	}

	@Override
	public String getTrackingCodeFooter()
	{
		return getTrackingCode(Meta.TRACKING_CODE_FOOTER);
	}

	private String getTrackingCode(String trackingCodeAttr)
	{
		String trackingCode = getString(trackingCodeAttr);
		return trackingCode != null ? TRACKING_CODE_PREFIX + trackingCode + TRACKING_CODE_POSTFIX : null;
	}

	@Override
	public Integer getAggregateThreshold()
	{
		return getInt(Meta.AGGREGATE_THRESHOLD);
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
		private static final String JS_HREF = "js_href";
		private static final String TRACKING_CODE_HEADER = "tracking_code_header";
		private static final String TRACKING_CODE_FOOTER = "tracking_code_footer";
		private static final String AGGREGATE_THRESHOLD = "aggregate_threshold";

		private static final String DEFAULT_TITLE = "MOLGENIS";
		private static final String DEFAULT_LOGO_NAVBAR_HREF = "/img/logo_molgenis_small.png";
		private static final boolean DEFAULT_SIGNUP = false;
		private static final boolean DEFAULT_SIGNUP_MODERATION = true;
		private static final String DEFAULT_LANGUAGE_CODE = "en";
		private static final String DEFAULT_BOOTSTRAP_THEME = "bootstrap-molgenis.min.css";

		public Meta()
		{
			super(ID);
			setLabel("Application settings");
			setDescription("General application settings.");

			addAttribute(TITLE).setDataType(STRING).setNillable(false).setDefaultValue(DEFAULT_TITLE)
					.setLabel("Application title").setDescription("Displayed in browser toolbar.");
			addAttribute(SIGNUP).setDataType(BOOL).setNillable(false).setDefaultValue(DEFAULT_SIGNUP)
					.setLabel("Allow users to sign up");
			addAttribute(SIGNUP).setDataType(BOOL).setNillable(false).setDefaultValue(DEFAULT_SIGNUP_MODERATION)
					.setLabel("Sign up moderation")
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
			addAttribute(JS_HREF).setDataType(STRING).setNillable(true).setLabel("JS href")
					.setDescription("JS file name to add custom JS (see molgenis-core-ui/src/main/resources/js).");
			addAttribute(TRACKING_CODE_HEADER).setDataType(SCRIPT).setNillable(true).setLabel("Tracking code header")
					.setDescription("JS tracking code (e.g. Google Analytics) that is placed in the header HTML.");
			addAttribute(TRACKING_CODE_FOOTER).setDataType(SCRIPT).setNillable(true).setLabel("Tracking code footer")
					.setDescription("JS tracking code (e.g. Piwik) that is placed in the footer HTML.");
			addAttribute(AGGREGATE_THRESHOLD).setDataType(INT).setNillable(true).setLabel("Aggregate threshold")
					.setDescription("Aggregate values below the threshold are reported as the threshold.");
		}

		@Override
		protected Entity getDefaultSettings()
		{
			// FIXME workaround for https://github.com/molgenis/molgenis/issues/1810
			MapEntity defaultSettings = new MapEntity(this);
			defaultSettings.set(TITLE, DEFAULT_TITLE);
			defaultSettings.set(LOGO_NAVBAR_HREF, DEFAULT_LOGO_NAVBAR_HREF);
			defaultSettings.set(SIGNUP, DEFAULT_SIGNUP);
			defaultSettings.set(LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE);
			defaultSettings.set(BOOTSTRAP_THEME, DEFAULT_BOOTSTRAP_THEME);
			return defaultSettings;
		}
	}
}
