package org.molgenis.dataexplorer.galaxy;

import java.io.IOException;

@Deprecated // FIXME extend from CodedRuntimeException
public class GalaxyDataExportException extends IOException
{
	private static final long serialVersionUID = 1L;

	public GalaxyDataExportException()
	{
		super();
	}

	public GalaxyDataExportException(String message)
	{
		super(message);
	}

	public GalaxyDataExportException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public GalaxyDataExportException(Throwable cause)
	{
		super(cause);
	}
}
