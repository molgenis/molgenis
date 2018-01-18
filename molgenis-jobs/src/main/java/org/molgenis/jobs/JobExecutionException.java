package org.molgenis.jobs;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
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
