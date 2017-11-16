package org.molgenis.oneclickimporter.exceptions;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class EmptySheetException extends Exception
{
	public EmptySheetException(String s)
	{
		super(s);
	}
}
