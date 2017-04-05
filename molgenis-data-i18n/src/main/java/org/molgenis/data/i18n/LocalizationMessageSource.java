package org.molgenis.data.i18n;

import org.molgenis.data.settings.AppSettings;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class LocalizationMessageSource extends AbstractMessageSource
{
	private final LocalizationService localizationService;
	private final String namespace;
	private final AppSettings appSettings;

	public LocalizationMessageSource(LocalizationService localizationService, String namespace, AppSettings appSettings)
	{
		super();
		this.localizationService = requireNonNull(localizationService);
		this.namespace = requireNonNull(namespace);
		this.appSettings = requireNonNull(appSettings);
		setAlwaysUseMessageFormat(false);
		setUseCodeAsDefaultMessage(false);
	}

	@Override
	protected String getDefaultMessage(String code)
	{
		return "#" + code + "#";
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale)
	{
		return new MessageFormat(resolveCodeWithoutArguments(code, locale));
	}

	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale)
	{
		String result = localizationService.getMessage(namespace, code, locale.getLanguage());
		if (result == null && appSettings.getLanguageCode() != null)
		{
			result = localizationService.getMessage(namespace, code, appSettings.getLanguageCode());
		}
		if (result == null)
		{
			result = localizationService.getMessage(namespace, code, LanguageService.DEFAULT_LANGUAGE_CODE);
		}
		return result;
	}
}
