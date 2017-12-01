package org.molgenis.data.mapper.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Use when an mapping algorithm is applied to a null value.
 */
public class AlgorithmNullValueException extends MappingServiceException
{
	private static final String ERROR_CODE = "M08";

	public AlgorithmNullValueException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "null";
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .orElseGet(super::getLocalizedMessage);
	}
}
