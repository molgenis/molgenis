package org.molgenis.data.annotation.core.exception;

@Deprecated // FIXME extend from CodedRuntimeException
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
