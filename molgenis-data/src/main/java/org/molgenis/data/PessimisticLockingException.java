package org.molgenis.data;

public class PessimisticLockingException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public PessimisticLockingException()
	{
	}

	public PessimisticLockingException(String msg)
	{
		super(msg);
	}

	public PessimisticLockingException(Throwable t)
	{
		super(t);
	}

	public PessimisticLockingException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
