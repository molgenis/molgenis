package org.molgenis.data;

public class MolgenisDataAccessException extends MolgenisRuntimeException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisDataAccessException()
	{
	}

	public MolgenisDataAccessException(String msg)
	{
		super(msg);
	}

	public MolgenisDataAccessException(Throwable t)
	{
		super(t);
	}

	public MolgenisDataAccessException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
