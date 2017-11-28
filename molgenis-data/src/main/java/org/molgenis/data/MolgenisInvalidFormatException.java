package org.molgenis.data;

@Deprecated // FIXME extend from CodedRuntimeException
public class MolgenisInvalidFormatException extends MolgenisRuntimeException
{
	private static final long serialVersionUID = 1L;

	public MolgenisInvalidFormatException(String message)
	{
		super(message);
	}

	public MolgenisInvalidFormatException(String message, Exception cause)
	{
		super(message, cause);
	}
}
