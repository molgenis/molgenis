package org.molgenis.data.security;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSecurityL10nConfig
{
	public static final String NAMESPACE = "data_security";

	@Bean
	public PropertiesMessageSource dataSecurityMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
