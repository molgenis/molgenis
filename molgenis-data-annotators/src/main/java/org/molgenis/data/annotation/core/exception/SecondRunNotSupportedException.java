package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class SecondRunNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN10";
	private RepositoryAnnotator annotator;

	public SecondRunNotSupportedException(RepositoryAnnotator annotator)
	{
		super(ERROR_CODE);
		this.annotator = annotator;
	}

	@Override
	public String getMessage()
	{
		return String.format("annotator:%s", annotator.getFullName());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, annotator.getFullName());
		}).orElse(super.getLocalizedMessage());
	}
}
