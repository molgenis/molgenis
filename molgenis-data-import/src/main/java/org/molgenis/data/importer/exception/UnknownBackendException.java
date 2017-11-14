package org.molgenis.data.importer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownBackendException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "I02";
	private final String backendName;

	public UnknownBackendException(String backendName)
	{
		super(ERROR_CODE);
		this.backendName = backendName;
	}

	@Override
	public String getMessage()
	{
		return String.format("name:%s", backendName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, backendName);
		}).orElse(super.getLocalizedMessage());
	}
}
