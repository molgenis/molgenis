package org.molgenis.data.validation;

import org.molgenis.data.i18n.PropertiesMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataValidationL10nConfig
{
	private static final String NAMESPACE = "data_validation";

	@Bean
	public PropertiesMessageSource dataValidationMessageSource()
	{
		return new PropertiesMessageSource(NAMESPACE);
	}
}
