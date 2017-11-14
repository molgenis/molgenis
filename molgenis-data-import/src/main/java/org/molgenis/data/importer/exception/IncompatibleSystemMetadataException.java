package org.molgenis.data.importer.exception;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IncompatibleSystemMetadataException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "I04";

	public IncompatibleSystemMetadataException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return String.format("Incompatible SystemEntityMetadata");
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .orElse(super.getLocalizedMessage());
	}
}
