package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class UnknownAttributeException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public UnknownAttributeException()
	{
	}

	public UnknownAttributeException(String message)
	{
		super(message);
	}

	public UnknownAttributeException(Throwable cause)
	{
		super(cause);
	}

	public UnknownAttributeException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
