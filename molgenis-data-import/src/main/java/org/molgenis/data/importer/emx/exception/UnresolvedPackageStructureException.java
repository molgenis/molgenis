package org.molgenis.data.importer.emx.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnresolvedPackageStructureException extends EmxException
{
	private final static String ERROR_CODE = "E09";

	public UnresolvedPackageStructureException()
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
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .orElse(super.getLocalizedMessage());
	}
}
