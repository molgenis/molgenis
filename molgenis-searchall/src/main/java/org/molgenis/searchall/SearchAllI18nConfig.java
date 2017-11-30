package org.molgenis.searchall;

import org.molgenis.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchAllI18nConfig
{

	public static final String NAMESPACE = "searchall";

	@Bean
	public PropertiesMessageSource searchallMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
