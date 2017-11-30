package org.molgenis.data.index;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexI18nConfig
{

	public static final String NAMESPACE = "index";

	@Bean
	public PropertiesMessageSource indexMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
