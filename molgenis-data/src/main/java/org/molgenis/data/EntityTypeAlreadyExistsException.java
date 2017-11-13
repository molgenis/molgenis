package org.molgenis.data;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EntityTypeAlreadyExistsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D03";

	private final String entityTypeId;

	public EntityTypeAlreadyExistsException(String entityTypeId)
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
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .map(format -> MessageFormat.format(format, entityTypeId))
								   .orElse(super.getLocalizedMessage());
	}
}
