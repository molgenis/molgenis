package org.molgenis.gavin.job;

@Deprecated // FIXME extend from CodedRuntimeException
public class JobNotFoundException extends Exception
{
	public JobNotFoundException(String msg)
	{
		super(msg);
	}
}
