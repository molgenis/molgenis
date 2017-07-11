package org.molgenis.data.security.acl;

import org.molgenis.security.core.Permission;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;

/**
 * Maps domain object permission to/from entity permission.
 */
@Component
class PermissionMapper
{
	org.springframework.security.acls.model.Permission toDomainPermission(Permission permission)
	{
		switch (permission)
		{
			case READ:
				return BasePermission.READ;
			case WRITE:
				return BasePermission.WRITE;
			case COUNT:
			case NONE:
				throw new IllegalArgumentException(String.format("Illegal permission '%s'", permission.toString()));
			case WRITEMETA:
				return BasePermission.ADMINISTRATION;
			default:
				throw new RuntimeException(String.format("Unknown permission '%s'", permission.toString()));
		}
	}

	Permission toPermission(org.springframework.security.acls.model.Permission permission)
	{
		if (!(permission instanceof BasePermission))
		{
			throw new RuntimeException("Permission is not a BasePermission");
		}

		BasePermission basePermission = (BasePermission) permission;
		if (basePermission == BasePermission.READ)
		{
			return Permission.READ;
		}
		else if (basePermission == BasePermission.WRITE)
		{
			return Permission.WRITE;
		}
		else
		{
			throw new RuntimeException(
					String.format("BasePermission '%s' cannot be mapped to Permission", basePermission));
		}
	}
}
