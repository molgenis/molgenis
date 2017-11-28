package org.molgenis.data.importer.wizard.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EntityTypePermissionsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C01";
	private final Permission permission;
	private EntityType entityType;

	public EntityTypePermissionsException(EntityType entityType, Permission permission)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.permission = requireNonNull(permission);
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s, permission:%s", entityType.getId(), permission.name());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, entityType.getLabel(language), permission.name());
		}).orElseGet(super::getLocalizedMessage);
	}
}
