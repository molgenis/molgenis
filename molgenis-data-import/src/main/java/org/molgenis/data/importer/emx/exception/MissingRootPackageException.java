package org.molgenis.data.importer.emx.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingRootPackageException extends EmxException
{
	private final static String ERROR_CODE = "E11";

	public MissingRootPackageException()
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
