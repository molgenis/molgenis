package org.molgenis.data.importer;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImporterI18nConfig
{

	public static final String NAMESPACE = "importer";

	@Bean
	public PropertiesMessageSource importerMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
