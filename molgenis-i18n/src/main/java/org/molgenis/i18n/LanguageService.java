package org.molgenis.i18n;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceResourceBundle;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.molgenis.i18n.MessageSourceHolder.getMessageSource;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;

public class LanguageService
{
	private LanguageService()
	{
	}

	public static final String LANGUAGE_CODE_EN = "en";
	private static final String LANGUAGE_CODE_DE = "de";
	public static final String LANGUAGE_CODE_NL = "nl";
	private static final String LANGUAGE_CODE_ES = "es";
	private static final String LANGUAGE_CODE_IT = "it";
	private static final String LANGUAGE_CODE_PT = "pt";
	private static final String LANGUAGE_CODE_FR = "fr";
	private static final String LANGUAGE_CODE_XX = "xx";
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	public static final String DEFAULT_LANGUAGE_CODE = LANGUAGE_CODE_EN;
	public static final String DEFAULT_LANGUAGE_NAME = "English";

	/**
	 * "en": is default
	 * "xx": is a placeholder for having your own language
	 * "nl", "de", "es", "it", "pt", "fr": are extra languages
	 */
	public static Stream<String> getLanguageCodes()
	{
		return Stream.of(LANGUAGE_CODE_EN, LANGUAGE_CODE_NL, LANGUAGE_CODE_DE, LANGUAGE_CODE_ES, LANGUAGE_CODE_IT,
				LANGUAGE_CODE_PT, LANGUAGE_CODE_FR, LANGUAGE_CODE_XX);
	}

	public static boolean hasLanguageCode(String code)
	{
		return getLanguageCodes().anyMatch(languageCode -> languageCode.equals(code));
	}

	/**
	 * Creates a localization ResourceBundle for current locale.
	 *
	 * @return MessageSourceResourceBundle
	 */
	@Deprecated
	public static MessageSourceResourceBundle getBundle()
	{
		return new MessageSourceResourceBundle(getMessageSource(), getLocale());
	}

	/**
	 * @return the current user's language code
	 * @deprecated Use the {@link LocaleContextHolder} instead.
	 */
	@Deprecated
	public static String getCurrentUserLanguageCode()
	{
		return getLocale().getLanguage();
	}

	/**
	 * @deprecated Use {@link MessageSourceHolder} and {@link LocaleContextHolder} instead
	 */
	@Deprecated
	public static Optional<String> formatMessage(String code, Object[] arguments)
	{
		try
		{
			return Optional.of(getMessageSource().getMessage(code, arguments, getLocale()));
		}
		catch (NoSuchMessageException ex)
		{
			return Optional.empty();
		}
	}
}
