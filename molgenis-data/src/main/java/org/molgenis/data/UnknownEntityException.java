package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class UnknownEntityException extends MolgenisDataException
{
	private static final long serialVersionUID = 5202731000953612564L;

	public UnknownEntityException()
	{
	}

	public UnknownEntityException(String msg)
	{
		super(msg);
	}

	public UnknownEntityException(Throwable t)
	{
		super(t);
	}

	public UnknownEntityException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
