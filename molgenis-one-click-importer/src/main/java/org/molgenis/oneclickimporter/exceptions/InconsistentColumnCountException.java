package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InconsistentColumnCountException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI03";
	private String filename;

	public InconsistentColumnCountException(String filename)
	{
		super(ERROR_CODE);
		this.filename = requireNonNull(filename);
	}

	@Override
	public String getMessage()
	{
		return String.format("filename:%s", filename);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, filename);
		}).orElse(super.getLocalizedMessage());
	}
}
