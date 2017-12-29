package org.molgenis.settings.controller;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsI18nConfig
{

	public static final String NAMESPACE = "settings";

	@Bean
	public PropertiesMessageSource settingsMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
