package org.molgenis.settings.controller;

import org.molgenis.i18n.PropertiesMessageSource;

//@Configuration
public class SettingsI18nConfig
{

	public static final String NAMESPACE = "settings";

//	@Bean
	public PropertiesMessageSource settingsMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
