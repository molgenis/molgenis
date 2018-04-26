package org.molgenis.data.i18n;

import org.molgenis.data.DataService;
import org.molgenis.data.i18n.model.L10nStringFactory;
import org.molgenis.i18n.LocalizationMessageSource;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.settings.AppSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

@Configuration
public class LocalizationConfig
{
	@Autowired
	private L10nStringFactory l10nStringFactory;

	@Autowired
	private DataService dataService;

	@Autowired
	private AppSettings appSettings;

	@Autowired
	private MessageFormatFactory messageFormatFactory;

	@Bean
	public LocalizationPopulator localizationPopulator()
	{
		return new LocalizationPopulator(localizationRepository(), l10nStringFactory);
	}

	@Bean
	public LocalizationService localizationRepository()
	{
		return new LocalizationService(dataService, l10nStringFactory);
	}

	@Bean
	public MessageSource messageSource()
	{
		LocalizationMessageSource localizationMessageSource = new LocalizationMessageSource(messageFormatFactory,
				localizationRepository(), () -> new Locale(appSettings.getLanguageCode()));
		ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.addBasenames("org.hibernate.validator.ValidationMessages");
		localizationMessageSource.setParentMessageSource(resourceBundleMessageSource);
		MessageSourceHolder.setMessageSource(localizationMessageSource);
		return localizationMessageSource;
	}
}
