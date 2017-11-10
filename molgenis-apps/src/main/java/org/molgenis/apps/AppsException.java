package org.molgenis.apps;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class AppsException extends RuntimeException
{
	public AppsException(String message)
	{
		super(message);
	}
}
