package org.molgenis.oneclickimporter.exceptions;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
public class UnknownFileTypeException extends Exception
{
	public UnknownFileTypeException(String s)
	{
		super(s);
	}
}
