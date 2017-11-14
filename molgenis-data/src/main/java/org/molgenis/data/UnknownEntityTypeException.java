package org.molgenis.data;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownEntityTypeException extends UnknownDataException
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
		return getLanguageService().map(
				languageService -> MessageFormat.format(languageService.getString(ERROR_CODE), entityTypeId))
								   .orElse(super.getLocalizedMessage());
	}
}

