package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class WorkbookCreationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI04";

	public WorkbookCreationException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return format;
		}).orElse(super.getLocalizedMessage());
	}
}
