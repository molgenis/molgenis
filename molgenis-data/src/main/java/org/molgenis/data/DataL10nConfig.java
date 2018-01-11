package org.molgenis.data;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataL10nConfig
{
	public static final String NAMESPACE = "data";

	@Bean
	public PropertiesMessageSource dataMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
