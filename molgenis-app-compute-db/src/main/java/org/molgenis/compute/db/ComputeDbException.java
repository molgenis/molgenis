package org.molgenis.compute.db;

public class ComputeDbException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ComputeDbException()
	{
		super();
	}

	public ComputeDbException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	public ComputeDbException(String arg0)
	{
		super(arg0);
	}

	public ComputeDbException(Throwable arg0)
	{
		super(arg0);
	}

}
