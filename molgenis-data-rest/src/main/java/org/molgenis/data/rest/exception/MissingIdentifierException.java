package org.molgenis.data.rest.exception;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Exception thrown when an operation on a entity was attempted without specifying the identifier for the entity
 */
public class MissingIdentifierException extends RestApiException
{
	private final static String ERROR_CODE = "R09";
	private int count;

	public MissingIdentifierException(int count)
	{
		super(ERROR_CODE);
		this.count = requireNonNull(count);
	}

	@Override
	public String getMessage()
	{
		return String.format("index:%s", count);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, count);
		}).orElseGet(super::getLocalizedMessage);
	}
}