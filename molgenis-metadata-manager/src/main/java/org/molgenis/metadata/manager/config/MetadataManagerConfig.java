package org.molgenis.metadata.manager.config;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetadataManagerConfig
{
	@Bean
	public PropertiesMessageSource metadataManagerMessageSource()
	{
		return new PropertiesMessageSource("metadata-manager");
	}
}
