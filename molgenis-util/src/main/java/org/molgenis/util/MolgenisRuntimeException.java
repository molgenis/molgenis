package org.molgenis.util;

/**
 * @deprecated use class that extends from org.molgenis.i18n.CodedRuntimeException
 */
@Deprecated
public class MolgenisRuntimeException extends RuntimeException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisRuntimeException()
	{
	}

	public MolgenisRuntimeException(String msg)
	{
		super(msg);
	}

	public MolgenisRuntimeException(Throwable t)
	{
		super(t);
	}

	public MolgenisRuntimeException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
