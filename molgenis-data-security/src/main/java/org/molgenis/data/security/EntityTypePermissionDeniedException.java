package org.molgenis.data.security;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.util.UnexpectedEnumException;

import static java.text.MessageFormat.format;
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

	@Override
	public String getMessage()
	{
		return format("id:%s permission:%s", entityType.getId(), permission.name());
	}

	@Override
	public String getLocalizedMessage()
	{
		String format = getLanguageService().getBundle().getString(ERROR_CODE);
		String permissionMessage = getLanguageService().getBundle().getString(getPermissionKey(permission));
		String language = getLanguageService().getCurrentUserLanguageCode();
		return format(format, permissionMessage, entityType.getLabel(language));
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
