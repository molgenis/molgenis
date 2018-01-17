package org.molgenis.data.annotation.core.exception;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class UnresolvedAnnotatorDependencyException extends RuntimeException
{
	public UnresolvedAnnotatorDependencyException()
	{
	}

	public UnresolvedAnnotatorDependencyException(String msg)
	{
		super(msg);
	}

	public UnresolvedAnnotatorDependencyException(Throwable t)
	{
		super(t);
	}

	public UnresolvedAnnotatorDependencyException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
