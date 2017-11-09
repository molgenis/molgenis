package org.molgenis.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizedExceptionUtils
{
	private static final String BUNDLE_EXCEPTION = "exception";

	private LocalizedExceptionUtils()
	{
	}

	public static String getLocalizedBundleMessage(String bundleId, Locale locale, String messageKey)
	{
		return ResourceBundle.getBundle(BUNDLE_EXCEPTION + '_' + bundleId, locale).getString(messageKey);
	}
}
