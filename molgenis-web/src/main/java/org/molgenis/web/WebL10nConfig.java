package org.molgenis.web;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebL10nConfig
{
	@Bean
	PropertiesMessageSource webMessageSource()
	{
		return new PropertiesMessageSource("web");
	}
}
