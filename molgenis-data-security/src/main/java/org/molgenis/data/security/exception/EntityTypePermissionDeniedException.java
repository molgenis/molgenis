package org.molgenis.data.security.exception;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class EntityTypePermissionDeniedException extends PermissionDeniedException
{
	private static final String ERROR_CODE = "S01";

	private final transient EntityType entityType;
	private final Permission permission;

	public EntityTypePermissionDeniedException(EntityType entityType, Permission permission)
	{
		super(ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.permission = requireNonNull(permission);
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	public Permission getPermission()
	{
		return permission;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s permission:%s", entityType.getId(), permission.name());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String permissionName = getPermissionName(languageService, permission);
			MessageFormat format = languageService.getMessageFormat(ERROR_CODE);
			return format.format(new Object[] { permissionName, entityType });
		}).orElseGet(super::getLocalizedMessage);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		throw new UnsupportedOperationException();
	}

}
