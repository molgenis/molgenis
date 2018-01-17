package org.molgenis.semanticmapper.config;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticMapperI18nConfig
{
	public static final String NAMESPACE = "semantic-mapper";

	@Bean
	public PropertiesMessageSource semanticMapperMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
