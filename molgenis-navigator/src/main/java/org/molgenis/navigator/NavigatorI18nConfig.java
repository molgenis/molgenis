package org.molgenis.navigator;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NavigatorI18nConfig
{

	public static final String NAMESPACE = "navigator";

	@Bean
	public PropertiesMessageSource navigatorMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
