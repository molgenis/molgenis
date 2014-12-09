package org.molgenis.compute.ui;

public class ComputeUiException extends RuntimeException
{
	private static final long serialVersionUID = -1370382333357329334L;

	public ComputeUiException(String message)
	{
		super(message);
	}

	public ComputeUiException(Throwable cause)
	{
		super(cause);
	}

	public ComputeUiException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
