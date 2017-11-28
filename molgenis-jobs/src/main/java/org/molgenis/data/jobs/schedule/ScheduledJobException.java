package org.molgenis.data.jobs.schedule;

@Deprecated // FIXME extend from CodedRuntimeException
public class ScheduledJobException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ScheduledJobException()
	{
		super();
	}

	public ScheduledJobException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ScheduledJobException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ScheduledJobException(String message)
	{
		super(message);
	}

	public ScheduledJobException(Throwable cause)
	{
		super(cause);
	}
}
