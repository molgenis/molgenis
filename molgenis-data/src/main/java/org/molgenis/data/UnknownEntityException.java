package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:S1948")
public class UnknownEntityException extends UnknownDataException
{
	private static final String ERROR_CODE = "D02";

	private final EntityType entityType;
	private final Object entityId;

	public UnknownEntityException(EntityType entityType, Object entityId)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.entityId = requireNonNull(entityId);
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s id:%s", entityType.getId(), entityId.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, entityId.toString(), entityType.getLabel(language));
		}).orElse(super.getLocalizedMessage());
	}
}

