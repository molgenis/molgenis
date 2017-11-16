package org.molgenis.ui.style;

import org.molgenis.data.MolgenisException;

import java.io.IOException;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class MolgenisStyleException extends MolgenisException
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
