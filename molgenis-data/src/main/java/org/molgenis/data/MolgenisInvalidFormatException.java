package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
public class MolgenisInvalidFormatException extends Exception
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
