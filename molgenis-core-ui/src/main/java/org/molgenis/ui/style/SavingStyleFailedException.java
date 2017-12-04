package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class SavingStyleFailedException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "C09";

	private String fileName;
	private Throwable cause;

	public SavingStyleFailedException(String fileName, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.fileName = requireNonNull(fileName);
		this.cause = requireNonNull(cause);
	}

	@Override
	public String getMessage()
	{
		return String.format("fileName:%s cause:%s", fileName, cause.getMessage());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, fileName);
		}).orElseGet(super::getLocalizedMessage);
	}
}
