package org.molgenis.data;

public class DuplicateEntityException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public DuplicateEntityException(String msg)
	{
		super(msg);
	}

	public DuplicateEntityException(Throwable t)
	{
		super(t);
	}

	public DuplicateEntityException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
