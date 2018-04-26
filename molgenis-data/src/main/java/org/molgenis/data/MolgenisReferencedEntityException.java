package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class MolgenisReferencedEntityException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public MolgenisReferencedEntityException()
	{
	}

	public MolgenisReferencedEntityException(String message)
	{
		super(message);
	}

	public MolgenisReferencedEntityException(Throwable cause)
	{
		super(cause);
	}

	public MolgenisReferencedEntityException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
