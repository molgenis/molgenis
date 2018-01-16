package org.molgenis.script.core;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptsI18nConfig
{
	public static final String NAMESPACE = "scripts";

	@Bean
	public PropertiesMessageSource scriptsMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
