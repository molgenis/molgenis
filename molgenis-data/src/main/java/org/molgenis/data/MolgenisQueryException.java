package org.molgenis.data;

@Deprecated // FIXME extend from CodedRuntimeException
public class MolgenisQueryException extends MolgenisDataException
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
