package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidExpandException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "R03";
	private Object expand;

	public InvalidExpandException(String expand)
	{
		super(ERROR_CODE);
		this.expand = requireNonNull(expand);
	}

	@Override
	public String getMessage()
	{
		return String.format("expand:", expand);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, expand);
		}).orElse(super.getLocalizedMessage());
	}
}
