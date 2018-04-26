package org.molgenis.data;

/**
 * @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException}
 */
@Deprecated
public class MolgenisRepositoryCapabilitiesException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public MolgenisRepositoryCapabilitiesException(String msg)
	{
		super(msg);
	}

	public MolgenisRepositoryCapabilitiesException(Throwable t)
	{
		super(t);
	}

	public MolgenisRepositoryCapabilitiesException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
