package org.molgenis.app.manager.exception;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class AppManagerException extends RuntimeException
{
	public AppManagerException(String message)
	{
		super(message);
	}
}
