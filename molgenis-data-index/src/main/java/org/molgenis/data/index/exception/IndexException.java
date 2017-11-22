package org.molgenis.data.index.exception;

import org.molgenis.data.MolgenisDataException;

@Deprecated // FIXME extend from CodedRuntimeException
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
