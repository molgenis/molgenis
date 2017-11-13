package org.molgenis.data.i18n;

import org.springframework.context.support.MessageSourceResourceBundle;

import java.util.stream.Stream;

public interface LanguageService
{
	String LANGUAGE_CODE_EN = "en";
	String LANGUAGE_CODE_NL = "nl";
	String LANGUAGE_CODE_DE = "de";
	String LANGUAGE_CODE_ES = "es";
	String LANGUAGE_CODE_IT = "it";
	String LANGUAGE_CODE_PT = "pt";
	String LANGUAGE_CODE_FR = "fr";
	String LANGUAGE_CODE_XX = "xx";

	/**
	 * "en": is default
	 * "xx": is a placeholder for having your own language
	 * "nl", "de", "es", "it", "pt", "fr": are extra languages
	 */
	static Stream<String> getLanguageCodes()
	{
		return Stream.of(LANGUAGE_CODE_EN, LANGUAGE_CODE_NL, LANGUAGE_CODE_DE, LANGUAGE_CODE_ES, LANGUAGE_CODE_IT,
				LANGUAGE_CODE_PT, LANGUAGE_CODE_FR, LANGUAGE_CODE_XX);
	}

	MessageSourceResourceBundle getBundle();

	String getCurrentUserLanguageCode();
}
