package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnresolvedAnnotatorDependencyException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN07";
	private String annotatorName;

	public UnresolvedAnnotatorDependencyException(String annotatorName)
	{
		super(ERROR_CODE);
		this.annotatorName = requireNonNull(annotatorName);
	}

	@Override
	public String getMessage()
	{
		return String.format("annotatorName:%s", annotatorName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, annotatorName);
		}).orElse(super.getLocalizedMessage());
	}
}
