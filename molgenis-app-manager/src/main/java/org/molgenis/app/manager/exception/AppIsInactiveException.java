package org.molgenis.app.manager.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class AppIsInactiveException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AM07";
	private final String uri;

	public AppIsInactiveException(String uri)
	{
		super(ERROR_CODE);
		this.uri = requireNonNull(uri);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { uri };
	}

	@Override
	public String getMessage()
	{
		return String.format("uri:%s", uri);
	}
}
