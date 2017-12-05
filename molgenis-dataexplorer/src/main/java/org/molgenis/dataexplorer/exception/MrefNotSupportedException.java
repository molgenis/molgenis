package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MrefNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DE03";
	private String subject;

	public MrefNotSupportedException(String subject)
	{
		super(ERROR_CODE);
		this.subject = requireNonNull(subject);
	}

	public String getSubject()
	{
		return subject;
	}

	@Override
	public String getMessage()
	{
		return String.format("subject:%s", subject);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, subject);
		}).orElseGet(super::getLocalizedMessage);
	}
}
