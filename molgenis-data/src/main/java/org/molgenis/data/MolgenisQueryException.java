package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class MolgenisQueryException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public MolgenisQueryException()
	{
	}

	public MolgenisQueryException(String msg)
	{
		super(msg);
	}

	public MolgenisQueryException(Throwable t)
	{
		super(t);
	}

	public MolgenisQueryException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
