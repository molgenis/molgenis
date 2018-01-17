package org.molgenis.data.importer.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnknownImportModeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "I05";
	private final String action;

	public UnknownImportModeException(String action)
	{
		super(ERROR_CODE);
		this.action = requireNonNull(action);
	}

	public String getAction()
	{
		return action;
	}

	@Override
	public String getMessage()
	{
		return String.format("action:%s", action);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { action };
	}
}
