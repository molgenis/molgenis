package org.molgenis.data;

@Deprecated // FIXME implement ErrorCoded + refactor uses + rename to CodedException
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
