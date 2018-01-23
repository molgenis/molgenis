package org.molgenis.i18n;

import org.molgenis.i18n.format.MessageFormatFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class I18nConfig
{
	@Bean
	public MessageFormatFactory messageFormatFactory()
	{
		return new MessageFormatFactory();
	}
}
