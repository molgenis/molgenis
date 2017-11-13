package org.molgenis.data;

import org.molgenis.data.i18n.LanguageServiceHolder;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;

public class UnknownEntityTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D01";

	private final String entityTypeId;

	public UnknownEntityTypeException(String entityTypeId)
	{
		super(ERROR_CODE);
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", entityTypeId);
	}

	@Override
	public String getLocalizedMessage()
	{
		String format = LanguageServiceHolder.getLanguageService().getString(ERROR_CODE);
		return MessageFormat.format(format, entityTypeId);
	}
}

