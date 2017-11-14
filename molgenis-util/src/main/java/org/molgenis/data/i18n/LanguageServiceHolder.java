package org.molgenis.data.i18n;

import java.util.Optional;

public class LanguageServiceHolder
{
	private static LanguageService LANGUAGE_SERVICE;

	public static Optional<LanguageService> getLanguageService()
	{
		return Optional.ofNullable(LANGUAGE_SERVICE);
	}

	public static void setLanguageService(LanguageService languageService)
	{
		LANGUAGE_SERVICE = languageService;
	}
}