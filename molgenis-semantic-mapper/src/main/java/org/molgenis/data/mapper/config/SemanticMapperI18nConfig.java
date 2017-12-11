package org.molgenis.data.mapper.config;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticMapperI18nConfig
{

	public static final String NAMESPACE = "semantic-mapper";

	@Bean
	public PropertiesMessageSource importerMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
