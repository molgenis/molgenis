package org.molgenis.data.annotation.core;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotatorI18nConfig
{
	public static final String NAMESPACE = "annotators";

	@Bean
	public PropertiesMessageSource annotatorMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
