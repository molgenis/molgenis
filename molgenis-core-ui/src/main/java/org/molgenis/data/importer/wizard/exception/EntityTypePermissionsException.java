package org.molgenis.data.importer.wizard.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import static java.util.Objects.requireNonNull;

public class EntityTypePermissionsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C01";
	private final Permission permission;
	private final EntityType entityType;

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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { entityType, permission.name() };
	}
}
