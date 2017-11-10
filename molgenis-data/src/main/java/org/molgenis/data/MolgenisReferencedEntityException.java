package org.molgenis.data;

@Deprecated // FIXME extend from LocalizedRuntimeException
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
