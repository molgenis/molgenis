package org.molgenis.data.rest;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestI18nConfig
{

	public static final String NAMESPACE = "rest";

	@Bean
	public PropertiesMessageSource importerMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
