package org.molgenis.oneclickimporter.exceptions;

@Deprecated // FIXME extend from CodedRuntimeException
public class UnknownFileTypeException extends Exception
{
	public UnknownFileTypeException(String s)
	{
		super(s);
	}
}
