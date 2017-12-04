package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidNumberOFGenesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN03";
	private int sourceEntitiesSize;

	public InvalidNumberOFGenesException(int sourceEntitiesSize)
	{
		super(ERROR_CODE);
		this.sourceEntitiesSize = sourceEntitiesSize;
	}

	@Override
	public String getMessage()
	{
		return String.format("sourceEntitiesSize:%d", sourceEntitiesSize);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, sourceEntitiesSize);
		}).orElse(super.getLocalizedMessage());
	}
}
