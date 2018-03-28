package org.molgenis.data.row.edit.controller;

import org.molgenis.i18n.PropertiesMessageSource;

//@Configuration
public class DataRowEditI18nConfig
{

	public static final String NAMESPACE = "data-row-edit";

//	@Bean
	public PropertiesMessageSource settingsMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
