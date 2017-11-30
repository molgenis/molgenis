package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EntityTypePermissionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D14";
	private Permission permission;
	private EntityType entityType;

	public EntityTypePermissionException(Permission permission, EntityType entityType)
	{
		super(ERROR_CODE);
		this.permission = requireNonNull(permission);
		this.entityType = requireNonNull(entityType);
	}

	public Permission getPermission()
	{
		return permission;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	@Override
	public String getMessage()
	{
		return String.format("permission:%s type:%s", permission.name(), entityType.getId());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, permission.name(), entityType.getLabel(language));
		}).orElseGet(super::getLocalizedMessage);
	}
}
