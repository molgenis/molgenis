package org.molgenis.data;

import org.molgenis.util.MolgenisRuntimeException;

@Deprecated // FIXME extend from CodedRuntimeException
public class MolgenisDataException extends MolgenisRuntimeException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisDataException()
	{
		this("");
	}

	public MolgenisDataException(String msg)
	{
		super(msg);
	}

	public MolgenisDataException(Throwable t)
	{
		this("", t);
	}

	public MolgenisDataException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
