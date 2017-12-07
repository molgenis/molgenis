package org.molgenis.ui;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreUi18nConfig
{

	public static final String NAMESPACE = "core-ui";

	@Bean
	public PropertiesMessageSource coreUiMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
