package org.molgenis.security;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityI18nConfig
{

	public static final String NAMESPACE = "security";

	@Bean
	public PropertiesMessageSource importerMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
