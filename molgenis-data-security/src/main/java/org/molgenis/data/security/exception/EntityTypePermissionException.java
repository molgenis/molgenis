package org.molgenis.data.security.exception;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class EntityTypePermissionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DS04";
	private final EntityTypePermission permission;
	private final transient String entityTypeId;

	public EntityTypePermissionException(EntityTypePermission permission, String entityTypeId)
	{
		super(ERROR_CODE);
		this.permission = requireNonNull(permission);
		this.entityTypeId = requireNonNull(entityTypeId);
	}

	public EntityTypePermissionException(EntityTypePermission permission, EntityType entityType)
	{
		this(permission, entityType.getId());
	}

	@Override
	public String getMessage()
	{
		return String.format("permission:%s entityTypeId:%s", permission, entityTypeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { permission.getName(), entityTypeId };
	}
}