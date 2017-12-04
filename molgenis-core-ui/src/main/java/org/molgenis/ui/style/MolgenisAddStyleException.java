package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MolgenisAddStyleException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "C09";
	private String identifier;
	private Throwable cause;

	public MolgenisAddStyleException(String identifier, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.identifier = requireNonNull(identifier);
		this.cause = requireNonNull(cause);
	}

	@Override
	public String getMessage()
	{
		return String.format("identifier:%s cause:%s", identifier, cause.getMessage());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, identifier);
		}).orElseGet(super::getLocalizedMessage);
	}
}
