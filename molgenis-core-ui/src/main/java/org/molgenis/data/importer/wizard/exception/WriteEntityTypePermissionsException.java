package org.molgenis.data.importer.wizard.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class WriteEntityTypePermissionsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C01";
	private EntityType entityType;

	public WriteEntityTypePermissionsException(EntityType entityType)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s", entityType.getId());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, entityType.getLabel(language));
		}).orElseGet(super::getLocalizedMessage);
	}
}
