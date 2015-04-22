package org.molgenis.data.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityNameValidator
{
	private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

	public static boolean isValid(String value)
	{
		if (value == null)
		{
			return true;
		}

		Matcher m = PATTERN.matcher(value);
		return m.matches();
	}
}
