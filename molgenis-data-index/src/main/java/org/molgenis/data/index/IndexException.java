package org.molgenis.data.index;

import org.molgenis.data.MolgenisDataException;

public class IndexException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public IndexException(String msg)
	{
		super(msg);
	}

	public IndexException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
