package org.molgenis.gavin.job;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class JobNotFoundException extends Exception
{
	public JobNotFoundException(String msg)
	{
		super(msg);
	}
}
