package org.molgenis.data;

@Deprecated // FIXME extend from LocalizedRuntimeException
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
