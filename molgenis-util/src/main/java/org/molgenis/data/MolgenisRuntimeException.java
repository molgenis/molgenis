package org.molgenis.data;

@Deprecated // FIXME extend from CodedRuntimeException
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
