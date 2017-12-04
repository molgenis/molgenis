package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownFileTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI06";
	private String extension;

	public UnknownFileTypeException(String extension)
	{
		super(ERROR_CODE);
		this.extension = requireNonNull(extension);
	}

	@Override
	public String getMessage()
	{
		return String.format("extension:%s", extension);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, extension);
		}).orElse(super.getLocalizedMessage());
	}
}
