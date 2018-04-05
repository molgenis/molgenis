package org.molgenis.data.row.edit.controller;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataRowEditI18nConfig
{

	public static final String NAMESPACE = "data-row-edit";

	@Bean
	public PropertiesMessageSource settingsMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
