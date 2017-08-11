package org.molgenis.ui.style;

import java.io.IOException;

public class MolgenisStyleException extends Exception
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
