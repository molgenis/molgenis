package org.molgenis.data;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import static java.util.Objects.requireNonNull;

public class EntityTypePermissionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D14";
	private final Permission permission;
	private final EntityType entityType;

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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { permission.name(), entityType };
	}
}
