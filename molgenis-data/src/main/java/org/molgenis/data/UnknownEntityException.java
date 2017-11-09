package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.util.LocalizedExceptionUtils.getLocalizedBundleMessage;

public class UnknownEntityException extends MolgenisRuntimeException
{
	private static final String BUNDLE_ID = "data";
	private static final String BUNDLE_MESSAGE_KEY = "unknown_entity_message";

	private static final String MESSAGE_FORMAT = "id:%s typeId:%s";

	private final EntityType entityType;
	private final Object entityId;

	public UnknownEntityException(EntityType entityType, Object entityId)
	{
		this.entityType = requireNonNull(entityType);
		this.entityId = requireNonNull(entityId);
	}

	@Override
	public String getMessage()
	{
		return format(MESSAGE_FORMAT, entityType.getId(), entityId.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLocalizedMessage(LocaleContextHolder.getLocale());
	}

	private String getLocalizedMessage(Locale locale)
	{
		String messageFormat = getLocalizedBundleMessage(BUNDLE_ID, locale, BUNDLE_MESSAGE_KEY);
		String language = LocaleContextHolder.getLocale().getLanguage();
		return format(messageFormat, entityId.toString(), entityType.getLabel(language));
	}

}

