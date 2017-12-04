package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EmptySheetException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI01";
	private String sheettype;
	private String fileName;

	public EmptySheetException(String sheettype, String fileName)
	{
		super(ERROR_CODE);
		this.sheettype = requireNonNull(sheettype);
		this.fileName = requireNonNull(fileName);
	}

	@Override
	public String getMessage()
	{
		return String.format("sheettype:%s fileName:%s", sheettype, fileName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, sheettype, fileName);
		}).orElse(super.getLocalizedMessage());
	}
}
