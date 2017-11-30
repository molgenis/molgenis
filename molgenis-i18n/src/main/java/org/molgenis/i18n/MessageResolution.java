package org.molgenis.i18n;

import java.util.Locale;

public interface MessageResolution
{
	String resolveCodeWithoutArguments(String code, Locale locale);
}
