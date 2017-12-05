package org.molgenis.gavin;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GavinI18nConfig
{

	public static final String NAMESPACE = "gavin";

	@Bean
	public PropertiesMessageSource searchallMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
