package org.molgenis.data;

import static java.text.MessageFormat.format;
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
		String format = getLanguageService().getBundle().getString(ERROR_CODE);
		return format(format, entityTypeId);
	}
}
