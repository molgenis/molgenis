package org.molgenis.data.rest;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestI18nConfig
{
	public static final String NAMESPACE = "rest";

	@Bean
	public PropertiesMessageSource restMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}