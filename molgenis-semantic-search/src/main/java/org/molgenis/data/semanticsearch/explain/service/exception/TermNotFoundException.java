package org.molgenis.data.semanticsearch.explain.service.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class TermNotFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "Q01";
	private String description;

	public TermNotFoundException(String description)
	{
		super(ERROR_CODE);
		this.description = description;
	}

	@Override
	public String getMessage()
	{
		return String.format("description:%s", description);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, description);
		}).orElse(super.getLocalizedMessage());
	}
}