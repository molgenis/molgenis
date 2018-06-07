package org.molgenis.app.manager.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class AppIsInactiveException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AM07";
	private final String appName;

	public AppIsInactiveException(String appName)
	{
		super(ERROR_CODE);
		this.appName = requireNonNull(appName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { appName };
	}

	@Override
	public String getMessage()
	{
		return String.format("appName:%s", appName);
	}
}
