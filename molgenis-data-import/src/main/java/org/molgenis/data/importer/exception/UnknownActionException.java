package org.molgenis.data.importer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownActionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "I05";
	private final String action;

	public UnknownActionException(String action)
	{
		super(ERROR_CODE);
		this.action = action;
	}

	@Override
	public String getMessage()
	{
		return String.format("action:%s", action);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, action);
		}).orElse(super.getLocalizedMessage());
	}
}
