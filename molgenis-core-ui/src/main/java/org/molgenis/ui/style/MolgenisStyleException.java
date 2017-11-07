package org.molgenis.ui.style;

import org.molgenis.data.MolgenisRuntimeException;

import java.io.IOException;

public class MolgenisStyleException extends MolgenisRuntimeException
{
	public MolgenisStyleException(String s)
	{
		super(s);
	}

	public MolgenisStyleException(String s, IOException e)
	{
		super(s, e);
	}
}
