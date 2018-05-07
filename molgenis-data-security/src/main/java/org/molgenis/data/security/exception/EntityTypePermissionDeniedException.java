package org.molgenis.data.security.exception;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.EntityTypePermission;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityTypePermissionDeniedException extends PermissionDeniedException
{
	private static final String ERROR_CODE = "DS04";
	private static final String ERROR_CODE_WITHOUT_LABEL = "DS04a";
	private final EntityTypePermission permission;
	private final String entityTypeId;
	private final transient EntityType entityType;

	public EntityTypePermissionDeniedException(EntityTypePermission permission, String entityTypeId)
	{
		super(ERROR_CODE);
		this.permission = requireNonNull(permission);
		this.entityTypeId = requireNonNull(entityTypeId);
		this.entityType = null;
	}

	public EntityTypePermissionDeniedException(EntityTypePermission permission, EntityType entityType)
	{
		super(ERROR_CODE);
		this.permission = requireNonNull(permission);
		this.entityTypeId = requireNonNull(entityType.getId());
		this.entityType = Objects.requireNonNull(entityType);
	}

	@Override
	public String getMessage()
	{
		return String.format("permission:%s entityTypeId:%s", permission, entityTypeId);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { permission.getName(), entityTypeId, entityType };
	}

	@Override
	public String getErrorCode()
	{
		return entityType != null ? ERROR_CODE : ERROR_CODE_WITHOUT_LABEL;
	}
}
