package org.molgenis.data.i18n;

import org.molgenis.data.settings.AppSettings;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

/**
 * A {@link org.springframework.context.MessageSource} that allows you to look up messages in the LocalizationService.
 * <p>
 * Contains fallback logic that tries to fill in the best available value for a required Locale.
 * Marks missing values with # characters.
 * <p>
 * Caching is done by the {@link org.molgenis.data.DataService}.
 */
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

	/**
	 * The default message adds # marks around the code so that they stand out as not yet translated.
	 *
	 * @param code the untranslated code
	 * @return the code surrounded by `#` characters.
	 */
	@Override
	protected String getDefaultMessage(String code)
	{
		return "#" + code + "#";
	}

	/**
	 * Looks up the {@link MessageFormat} for a code.
	 *
	 * @param code   the code to look up
	 * @param locale the {@link Locale} for which the code should be looked up
	 * @return newly created {@link MessageFormat}
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale)
	{
		return new MessageFormat(resolveCodeWithoutArguments(code, locale));
	}

	/**
	 * Looks up a code in the {@link LocalizationService}.
	 * <p>
	 * First tries the given locale, then the language specified in the {@link AppSettings} and finally the default language.
	 *
	 * @param code   the messageID to look up.
	 * @param locale the Locale whose language code should be tried first.
	 * @return The message, or null if none found.
	 */
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
