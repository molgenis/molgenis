package org.molgenis.data.security;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;
import org.molgenis.util.LocalizedRuntimeException;
import org.molgenis.util.UnexpectedEnumException;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class EntityTypePermissionDeniedException extends LocalizedRuntimeException
{
	private static final String BUNDLE_ID = "data_security";
	private static final String ERROR_CODE = "S01";

	private final transient EntityType entityType;
	private final Permission permission;

	public EntityTypePermissionDeniedException(EntityType entityType, Permission permission)
	{
		super(BUNDLE_ID, ERROR_CODE);
		this.entityType = requireNonNull(entityType);
		this.permission = requireNonNull(permission);
	}

	@Override
	public String createMessage()
	{
		return format("id:%s permission:%s", entityType.getId(), permission.name());
	}

	@Override
	public String createLocalizedMessage(ResourceBundle resourceBundle, Locale locale)
	{
		String messageFormat = resourceBundle.getString("entity_type_permission_denied");
		String permissionMessage = resourceBundle.getString(getPermissionKey(permission));
		String language = locale.getLanguage();
		return format(messageFormat, permissionMessage, entityType.getLabel(language));
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
