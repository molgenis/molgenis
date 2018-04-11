package org.molgenis.apps;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class AppsException extends RuntimeException
{
	public AppsException(String message)
	{
		super(message);
	}
}
