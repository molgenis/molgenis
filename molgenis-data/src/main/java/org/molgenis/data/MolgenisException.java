package org.molgenis.data;

public class MolgenisException extends Exception
{
	public MolgenisException(String s)
	{
		super(s);
	}

	public MolgenisException(String s, Exception e)
	{
		super(s, e);
	}
}
