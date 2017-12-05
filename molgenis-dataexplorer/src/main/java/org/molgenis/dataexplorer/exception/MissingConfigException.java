package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MissingConfigException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DE03";
	private final String configName;

	public MissingConfigException(String configName)
	{
		super(ERROR_CODE);
		this.configName = requireNonNull(configName);
	}

	public String getConfigName()
	{
		return configName;
	}

	@Override
	public String getMessage()
	{
		return String.format("config:%s", configName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { configName };
	}
}
