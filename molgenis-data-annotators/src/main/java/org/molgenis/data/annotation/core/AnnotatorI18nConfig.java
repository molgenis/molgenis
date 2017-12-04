package org.molgenis.data.annotation.core;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnnotatorI18nConfig
{

	public static final String NAMESPACE = "annotators";

	@Bean
	public PropertiesMessageSource searchallMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
