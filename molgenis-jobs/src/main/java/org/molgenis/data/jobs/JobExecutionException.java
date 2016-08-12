package org.molgenis.data.jobs;

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
