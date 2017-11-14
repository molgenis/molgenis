package org.molgenis.data.importer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

//FIXME: reasonable name, or rewrite code that throws this to actually determine what is going on
public class NoSuitableImporterFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "I03";
	private final String fileName;

	public NoSuitableImporterFoundException(String fileName)
	{
		super(ERROR_CODE);
		this.fileName = fileName;
	}

	@Override
	public String getMessage()
	{
		return String.format("fileName:%s", fileName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, fileName);
		}).orElse(super.getLocalizedMessage());
	}
}
