package org.molgenis.security.permission;

import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;

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
					PluginIdentity.TYPE, PluginPermission.READ);
		}
	}

	@Override
	public boolean hasPermissionOnEntityType(String entityTypeId, Permission permission)
	{
		return hasPermission(entityTypeId, permission, AUTHORITY_ENTITY_PREFIX);
	}

	private boolean hasPermission(String authorityId, Permission permission, String authorityPrefix)
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) return false;

		String pluginAuthority = authorityPrefix + permission.toString() + '_' + authorityId;
		Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
		if (grantedAuthorities != null)
		{
			for (GrantedAuthority grantedAuthority : grantedAuthorities)
			{
				String authority = grantedAuthority.getAuthority();
				if (authority.equals(AUTHORITY_SU) || authority.equals(SystemSecurityToken.ROLE_SYSTEM)
						|| authority.equals(pluginAuthority)) return true;
			}
		}
		return false;
	}
}
