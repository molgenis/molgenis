package org.molgenis.app.manager.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class InvalidAppArchiveException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AM01";
	private final String archiveName;

	public InvalidAppArchiveException(String archiveName)
	{
		super(ERROR_CODE);
		this.archiveName = requireNonNull(archiveName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { archiveName };
	}
}
