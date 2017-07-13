package org.molgenis.security.permission;

import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.security.core.utils.SecurityUtils.*;

@Component
public class MolgenisPermissionServiceImpl implements MolgenisPermissionService
{
	private final EntityAclService entityAclService;

	public MolgenisPermissionServiceImpl(EntityAclService entityAclService)
	{
		this.entityAclService = requireNonNull(entityAclService);
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		return hasPermission(pluginId, permission, AUTHORITY_PLUGIN_PREFIX);
	}

	@Override
	public boolean hasPermissionOnEntity(String entityTypeId, Permission permission)
	{
		if (currentUserIsSuOrSystem())
		{
			return true;
		}
		return entityAclService.isGranted(EntityIdentity.create(ENTITY_TYPE_META_DATA, entityTypeId), permission);
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
				if (authority.equals(AUTHORITY_SU) || authority.equals(SystemSecurityToken.ROLE_SYSTEM) || authority
						.equals(pluginAuthority)) return true;
			}
		}
		return false;
	}
}
