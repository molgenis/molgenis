package org.molgenis.data.security.config;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSecurityConfig
{
	public static final String NAMESPACE = "data-security";

	@Bean
	public PropertiesMessageSource dataSecurityMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
