package org.molgenis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListEscapeUtils
{
	private static final int INITIAL_STRING_SIZE = 128;
	public static final char DEFAULT_SEPARATOR = ',';
	public static final char DEFAULT_ESCAPE_CHAR = '\\';

	private ListEscapeUtils()
	{
	}

	public static String toString(List<?> list)
	{
		return toString(list, DEFAULT_SEPARATOR, DEFAULT_ESCAPE_CHAR);
	}

	public static String toString(List<?> list, char sep)
	{
		return toString(list, sep, DEFAULT_ESCAPE_CHAR);
	}

	public static String toString(List<?> list, char sep, char esc)
	{
		if (sep == esc) throw new IllegalArgumentException("separator and escape char are equal");
		if (list == null) return null;

		StringBuilder strBuilder = new StringBuilder(INITIAL_STRING_SIZE);
		final int size = list.size();
		for (int i = 0; i < size; ++i)
		{
			if (i > 0) strBuilder.append(sep);
			Object obj = list.get(i);
			if (obj != null)
			{
				String str = obj.toString();
				if (str != null)
				{
					final int nrChars = str.length();
					for (int j = 0; j < nrChars; ++j)
					{
						char c = str.charAt(j);
						if (c == sep || c == esc) strBuilder.append(esc);
						strBuilder.append(c);
					}
				}
			}
		}
		return strBuilder.toString();
	}

	public static List<String> toList(String str)
	{
		return toList(str, DEFAULT_SEPARATOR, DEFAULT_ESCAPE_CHAR);
	}

	public static List<String> toList(String str, char sep)
	{
		return toList(str, sep, DEFAULT_ESCAPE_CHAR);
	}

	public static List<String> toList(String str, char sep, char esc)
	{
		if (sep == esc) throw new IllegalArgumentException("separator and escape char are equal");
		if (str == null) return null;
		if (str.isEmpty()) return Collections.emptyList();

		List<String> list = new ArrayList<>();

		StringBuilder strBuilder = new StringBuilder(INITIAL_STRING_SIZE);
		boolean isEscape = false;
		final int nrChars = str.length();
		for (int j = 0; j < nrChars; ++j)
		{
			char c = str.charAt(j);
			if (!isEscape)
			{
				if (c == esc)
				{
					if (j + 1 < nrChars)
					{
						char cnext = str.charAt(j + 1);
						if (cnext != sep && cnext != esc) strBuilder.append(c);
						else isEscape = true;
					}
					else
					{
						strBuilder.append(c);
					}
				}
				else if (c == sep)
				{
					list.add(strBuilder.toString().trim());
					strBuilder.setLength(0);
				}
				else
				{
					strBuilder.append(c);
				}
			}
			else
			{
				strBuilder.append(c);
				isEscape = false;
			}
		}
		list.add(strBuilder.toString().trim());

		return list;
	}
}
