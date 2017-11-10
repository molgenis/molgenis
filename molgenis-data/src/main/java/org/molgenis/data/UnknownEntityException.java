package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.LocalizedRuntimeException;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:S1948")
public class UnknownEntityException extends LocalizedRuntimeException
{
	private static final String BUNDLE_ID = "data";
	private static final String ERROR_CODE = "D01";

	private final EntityType entityType;
	private final Object entityId;

	public UnknownEntityException(EntityType entityType, Object entityId)
	{
		super(BUNDLE_ID, ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.entityId = requireNonNull(entityId);
	}

	@Override
	protected String createMessage()
	{
		return format("type:%s id:%s", entityType.getId(), entityId.toString());
	}

	@Override
	protected String createLocalizedMessage(ResourceBundle resourceBundle, Locale locale)
	{
		String format = resourceBundle.getString("unknown_entity_message");
		String language = locale.getLanguage();
		return format(format, entityId.toString(), entityType.getLabel(language));
	}
}

