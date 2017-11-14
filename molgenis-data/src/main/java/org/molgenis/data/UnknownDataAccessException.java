package org.molgenis.data;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * TODO discuss: extend from UncategorizedDataAccessException?
 */
public class UnknownDataAccessException extends DataAccessException
{
	private static final String ERROR_CODE = "D99";

	public UnknownDataAccessException(Throwable cause)
	{
		super(ERROR_CODE, cause);
	}

	@Override
	public String getMessage()
	{
		return "unknown error";
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .orElse(super.getLocalizedMessage());
	}
}
