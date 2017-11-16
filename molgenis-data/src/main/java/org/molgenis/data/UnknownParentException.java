package org.molgenis.data;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:S1948")
public class UnknownParentException extends UnknownDataException
{
	private static final String ERROR_CODE = "D08";

	private final Object entityId;
	private final Object parentEntityId;

	public UnknownParentException(Object entityId, Object parentEntityId)
	{
		super(ERROR_CODE);
		this.entityId = entityId;
		this.parentEntityId = parentEntityId;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s parentId: %s", entityId.toString(), parentEntityId.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, parentEntityId.toString(), entityId.toString());
		}).orElse(super.getLocalizedMessage());
	}
}

