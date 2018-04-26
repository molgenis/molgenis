package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class DuplicateEntityException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public DuplicateEntityException(String msg)
	{
		super(msg);
	}

	public DuplicateEntityException(Throwable t)
	{
		super(t);
	}

	public DuplicateEntityException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
