package org.molgenis.apps;

@Deprecated // FIXME extend from CodedRuntimeException
public class AppsException extends RuntimeException
{
	public AppsException(String message)
	{
		super(message);
	}
}
