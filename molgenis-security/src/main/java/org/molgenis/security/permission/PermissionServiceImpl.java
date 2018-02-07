package org.molgenis.security.permission;

import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.UnexpectedEnumException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Component
public class PermissionServiceImpl implements PermissionService
{
	private final PermissionEvaluator permissionEvaluator;

	public PermissionServiceImpl(PermissionEvaluator permissionEvaluator)
	{
		this.permissionEvaluator = requireNonNull(permissionEvaluator);
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return true;
		}
		else
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return authentication != null && permissionEvaluator.hasPermission(authentication, pluginId,
					PluginIdentity.TYPE, toPluginPermission(permission));
		}
	}

	private Object toPluginPermission(Permission permission)
	{
		switch (permission)
		{
			case COUNT:
				return PluginPermission.COUNT;
			case READ:
				return PluginPermission.READ;
			case WRITE:
				return PluginPermission.WRITE;
			case WRITEMETA:
				return PluginPermission.WRITEMETA;
			case NONE:
				throw new IllegalArgumentException(
						format("Permission evaluation for permission '%s' not allowed", permission.name()));
			default:
				throw new UnexpectedEnumException(permission);
		}
	}

	@Override
	public boolean hasPermissionOnEntityType(String entityTypeId, Permission permission)
	{
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return true;
		}
		else
		{
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			return authentication != null && permissionEvaluator.hasPermission(authentication, entityTypeId,
					EntityTypeIdentity.TYPE, toEntityTypePermission(permission));
		}
	}

	private Object toEntityTypePermission(Permission permission)
	{
		switch (permission)
		{
			case COUNT:
				return EntityTypePermission.COUNT;
			case READ:
				return EntityTypePermission.READ;
			case WRITE:
				return EntityTypePermission.WRITE;
			case WRITEMETA:
				return EntityTypePermission.WRITEMETA;
			case NONE:
				throw new IllegalArgumentException(
						format("Permission evaluation for permission '%s' not allowed", permission.name()));
			default:
				throw new UnexpectedEnumException(permission);
		}
	}
}
