package org.molgenis.jobs.schedule;

/**
 * @deprecated use class that extends from org.molgenis.i18n.CodedRuntimeException
 */
@Deprecated
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
