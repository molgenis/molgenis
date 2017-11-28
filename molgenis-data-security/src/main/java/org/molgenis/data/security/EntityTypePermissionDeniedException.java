package org.molgenis.data.security;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.util.UnexpectedEnumException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EntityTypePermissionDeniedException extends CodedRuntimeException
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
			String format = languageService.getString(ERROR_CODE);
			String permissionMessage = languageService.getString(getPermissionKey(permission));
			String language = languageService.getCurrentUserLanguageCode();
			return MessageFormat.format(format, permissionMessage, entityType.getLabel(language));
		}).orElseGet(super::getLocalizedMessage);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		throw new UnsupportedOperationException();
	}

	private static String getPermissionKey(Permission permission)
	{
		String messageKeyPostfix;
		switch (permission)
		{
			case READ:
				messageKeyPostfix = "read";
				break;
			case WRITE:
				messageKeyPostfix = "write";
				break;
			case COUNT:
				messageKeyPostfix = "count";
				break;
			case NONE:
				messageKeyPostfix = "none";
				break;
			case WRITEMETA:
				messageKeyPostfix = "writemeta";
				break;
			default:
				throw new UnexpectedEnumException(permission);
		}
		return "permission_" + messageKeyPostfix;
	}
}
