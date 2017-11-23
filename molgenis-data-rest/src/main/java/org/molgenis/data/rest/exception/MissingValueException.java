package org.molgenis.data.rest.exception;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Exception to be thrown if a vlue expected in the request was missing
 */
public class MissingValueException extends RestApiException
{
	private final static String ERROR_CODE = "R05";
	private String missing_value;
	private String location;

	public MissingValueException(String missing_value, String location)
	{
		super(ERROR_CODE);
		this.missing_value = requireNonNull(missing_value, location);
		this.location = location;
	}

	@Override
	public String getMessage()
	{
		return String.format("missing:%s", "location:%s", missing_value, location);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, missing_value, location);
		}).orElse(super.getLocalizedMessage());
	}
}
