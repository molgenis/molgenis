package org.molgenis.ui;

/**
 * Model attributes used in plugin views
 */
public class MolgenisPluginAttributes
{
	public static final String KEY_CONTEXT_URL = "context_url";
	public static final String KEY_PLUGIN_ID = "plugin_id";
	public static final String KEY_PLUGIN_ID_WITH_QUERY_STRING = "pluginid_with_query_string";
	public static final String KEY_MOLGENIS_UI = "molgenis_ui";
	public static final String KEY_AUTHENTICATED = "authenticated";
	/**
	 * environment: development, production
	 */
	public static final String KEY_ENVIRONMENT = "environment";
	public static final String KEY_RESOURCE_FINGERPRINT_REGISTRY = "resource_fingerprint_registry";
	public static final String KEY_APP_SETTINGS = "app_settings";
	public static final String KEY_PLUGIN_SETTINGS = "plugin_settings";
	/**
	 * Whether or not the current user can edit settings for the requested plugin
	 */
	public static final String KEY_PLUGIN_SETTINGS_CAN_WRITE = "plugin_settings_can_write";
	public static final String KEY_I18N = "i18n";

	private MolgenisPluginAttributes()
	{
	}
}
