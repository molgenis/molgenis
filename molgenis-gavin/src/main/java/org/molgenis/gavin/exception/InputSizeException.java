package org.molgenis.gavin.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InputSizeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G01";
	private int max_lines;

	public InputSizeException(int max_lines)
	{
		super(ERROR_CODE);

		this.max_lines = max_lines;
	}

	@Override
	public String getMessage()
	{
		return String.format("max_lines:%d", max_lines);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, max_lines);
		}).orElse(super.getLocalizedMessage());
	}
}
