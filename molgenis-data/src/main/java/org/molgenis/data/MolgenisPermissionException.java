package org.molgenis.data;

@Deprecated // FIXME extend from CodedRuntimeException
public class MolgenisPermissionException extends MolgenisRuntimeException
{
	private static final long serialVersionUID = 4738825795930038340L;

	public MolgenisPermissionException()
	{
	}

	public MolgenisPermissionException(String msg)
	{
		super(msg);
	}

	public MolgenisPermissionException(Throwable t)
	{
		super(t);
	}

	public MolgenisPermissionException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
