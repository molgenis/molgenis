package org.molgenis.data;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.util.LocalizedExceptionUtils.getLocalizedBundleMessage;

public class UnknownEntityTypeException extends MolgenisRuntimeException
{
	private static final String BUNDLE_ID = "data";
	private static final String MESSAGE_KEY = "unknown_entity_type_message";

	private final String entityTypeId;

	public UnknownEntityTypeException(String entityTypeId)
	{
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	@Override
	public String getMessage()
	{
		return format("id:%s", entityTypeId);
	}

	@Override
	public String getLocalizedMessage()
	{
		Locale locale = LocaleContextHolder.getLocale();
		String messageFormat = getLocalizedBundleMessage(BUNDLE_ID, locale, MESSAGE_KEY);
		return String.format(messageFormat, entityTypeId);
	}
}

