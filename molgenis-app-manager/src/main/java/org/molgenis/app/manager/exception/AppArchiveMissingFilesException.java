package org.molgenis.app.manager.exception;

import org.molgenis.i18n.CodedRuntimeException;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class AppArchiveMissingFilesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AM02";
	private final List<String> missingFromArchive;

	public AppArchiveMissingFilesException(List<String> missingFromArchive)
	{
		super(ERROR_CODE);
		this.missingFromArchive = requireNonNull(missingFromArchive);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { missingFromArchive };
	}

	@Override
	public String getMessage()
	{
		return String.format("missingFromArchive:%s", missingFromArchive);
	}

}
