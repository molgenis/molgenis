package org.molgenis.data.i18n;

public class LanguageServiceHolder
{
	private static LanguageService LANGUAGE_SERVICE;

	public static LanguageService getLanguageService()
	{
		return LANGUAGE_SERVICE;
	}

	public static void setLanguageService(LanguageService languageService)
	{
		LANGUAGE_SERVICE = languageService;
	}
}
