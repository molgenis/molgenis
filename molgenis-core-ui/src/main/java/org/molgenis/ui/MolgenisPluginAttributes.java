package org.molgenis.ui;

/**
 * Model attributes used in plugin views
 */
public class MolgenisPluginAttributes
{
	public static final String KEY_CONTEXT_URL = "context_url";
	static final String KEY_PLUGIN_ID = "plugin_id";
	static final String KEY_PLUGIN_ID_WITH_QUERY_STRING = "pluginid_with_query_string";
	static final String KEY_MOLGENIS_UI = "molgenis_ui";
	static final String KEY_AUTHENTICATED = "authenticated";
	/**
	 * environment: development, production
	 */
	static final String KEY_ENVIRONMENT = "environment";
	static final String KEY_RESOURCE_FINGERPRINT_REGISTRY = "resource_fingerprint_registry";
	static final String KEY_RESOURCE_UTILS = "resource_utils";
	static final String KEY_APP_SETTINGS = "app_settings";
	static final String KEY_AUTHENTICATION_SETTINGS = "authentication_settings";
	static final String KEY_PLUGIN_SETTINGS = "plugin_settings";
	/**
	 * Whether or not the current user can edit settings for the requested plugin
	 */
	static final String KEY_PLUGIN_SETTINGS_CAN_WRITE = "plugin_settings_can_write";
	static final String KEY_I18N = "i18n";

	private MolgenisPluginAttributes()
	{
	}
}
