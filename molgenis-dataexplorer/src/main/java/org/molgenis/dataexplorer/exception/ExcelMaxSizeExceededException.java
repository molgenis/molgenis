package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class ExcelMaxSizeExceededException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G02";
	private long size;

	public ExcelMaxSizeExceededException(long size)
	{
		super(ERROR_CODE);
		this.size = requireNonNull(size);
	}

	public long getSize()
	{
		return size;
	}

	@Override
	public String getMessage()
	{
		return String.format("size:%s", size);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, size);
		}).orElseGet(super::getLocalizedMessage);
	}
}
