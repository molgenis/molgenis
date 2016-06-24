package org.molgenis.data;

public class MolgenisDataException extends RuntimeException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisDataException()
	{
	}

	public MolgenisDataException(String msg)
	{
		super(msg);
	}

	public MolgenisDataException(Throwable t)
	{
		super(t);
	}

	public MolgenisDataException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
