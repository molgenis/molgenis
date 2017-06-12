package org.molgenis.data.index;

import org.molgenis.data.MolgenisDataException;

public class UnknownIndexException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public UnknownIndexException(String msg)
	{
		super(msg);
	}

	public UnknownIndexException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
