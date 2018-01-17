package org.molgenis.gavin;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GavinI18nConfig
{
	public static final String NAMESPACE = "gavin";

	@Bean
	public PropertiesMessageSource gavinMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
