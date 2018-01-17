package org.molgenis.oneclickimporter.exceptions;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class EmptySheetException extends Exception
{
	public EmptySheetException(String s)
	{
		super(s);
	}
}
