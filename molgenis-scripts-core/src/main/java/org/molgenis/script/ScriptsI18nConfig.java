package org.molgenis.script;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptsI18nConfig
{

	public static final String NAMESPACE = "scripts";

	@Bean
	public PropertiesMessageSource importerMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
