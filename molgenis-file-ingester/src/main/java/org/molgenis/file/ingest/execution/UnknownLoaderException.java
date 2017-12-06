package org.molgenis.file.ingest.execution;

import org.molgenis.data.CodedRuntimeException;

public class UnknownLoaderException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "F01";
	private final String loader;

	public UnknownLoaderException(String loader)
	{
		super(ERROR_CODE);
		this.loader = loader;
	}

	public String getLoader()
	{
		return loader;
	}

	@Override
	public String getMessage()
	{
		return String.format("loader:%s", loader);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { loader };
	}
}
