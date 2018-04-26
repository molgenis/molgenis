package org.molgenis.i18n;

public class I18nUtils
{
	private I18nUtils()
	{
	}

	/**
	 * Check is a name is suffixed with a language code
	 */
	public static boolean isI18n(String name)
	{
		return name.matches(".+-[a-z]{2,3}$");
	}

	/**
	 * Get the language code of a new with language suffix.
	 * <p>
	 * Returns null if not suffixed with language code
	 */
	public static String getLanguageCode(String name)
	{
		if (!isI18n(name)) return null;
		return name.substring(name.indexOf('-') + 1, name.length());
	}
}
