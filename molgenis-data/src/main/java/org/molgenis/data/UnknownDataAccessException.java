package org.molgenis.data;

import org.molgenis.data.i18n.LanguageServiceHolder;

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
		return LanguageServiceHolder.getLanguageService().getString(ERROR_CODE);
	}
}
