package org.molgenis.data.security.exception;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityPermission;
import org.molgenis.i18n.CodedRuntimeException;

public class EntityPermissionException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DS06";

	private final EntityPermission permission;
	private final EntityType entityType;
	private final Object entityId;

	public EntityPermissionException(EntityPermission permission, Entity entity)
	{
		super(ERROR_CODE);
		this.permission = permission;
		this.entityType = entity.getEntityType();
		this.entityId = entity.getIdValue();
	}

	@Override
	public String getMessage()
	{
		return String.format("permission:%s entityTypeId:%s entityId:%s", permission, entityType.getId(), entityId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { permission.getName(), entityId, entityType };
	}
}
