package org.molgenis.data.jobs;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class JobExecutionException extends RuntimeException
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public JobExecutionException(Exception cause)
	{
		super(cause);
	}

}
