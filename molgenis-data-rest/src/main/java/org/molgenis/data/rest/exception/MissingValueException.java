package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingValueException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "R05";
	private String missing_value;
	private String location;

	public MissingValueException(String missing_value, String location)
	{
		super(ERROR_CODE);
		this.missing_value = requireNonNull(missing_value);
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
