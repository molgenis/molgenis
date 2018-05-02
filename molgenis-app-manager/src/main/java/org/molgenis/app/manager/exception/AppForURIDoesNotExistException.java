package org.molgenis.app.manager.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class AppForURIDoesNotExistException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AM06";
	private final String uri;

	public AppForURIDoesNotExistException(String uri)
	{
		super(ERROR_CODE);
		this.uri = requireNonNull(uri);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { uri };
	}

	public String getUri()
	{
		return uri;
	}
}
