package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingDataException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI02";
	private String sheettype;
	private String filename;

	public MissingDataException(String sheettype, String filename)
	{
		super(ERROR_CODE);
		this.sheettype = requireNonNull(sheettype);
		this.filename = requireNonNull(filename);
	}

	@Override
	public String getMessage()
	{
		return String.format("sheettype:%s filename:%s", sheettype, filename);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, sheettype, filename);
		}).orElse(super.getLocalizedMessage());
	}
}
