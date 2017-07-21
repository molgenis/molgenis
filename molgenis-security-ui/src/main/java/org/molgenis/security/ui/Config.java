package org.molgenis.security.ui;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config
{
	private static final String NAMESPACE = "molgenis-security";

	@Bean
	public PropertiesMessageSource navigatorMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
