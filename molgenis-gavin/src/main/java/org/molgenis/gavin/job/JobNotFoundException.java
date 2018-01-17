package org.molgenis.gavin.job;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
public class JobNotFoundException extends Exception
{
	public JobNotFoundException(String msg)
	{
		super(msg);
	}
}
