package org.molgenis.security.permission;

import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.*;

@Component
public class MolgenisPermissionServiceImpl implements MolgenisPermissionService
{
	private final MutableAclService mutableAclService;

	public MolgenisPermissionServiceImpl(MutableAclService mutableAclService) {

		this.mutableAclService = requireNonNull(mutableAclService);
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

		ObjectIdentity objectIdentity = new ObjectIdentityImpl(EntityTypeMetadata.ENTITY_TYPE_META_DATA, entityTypeId);
		Acl acl = mutableAclService.readAclById(objectIdentity);
		boolean isGranted;
		try
		{
			Sid sid = new PrincipalSid(SecurityUtils.getCurrentUsername());
			isGranted = acl.isGranted(expandPermissions(permission),
					singletonList(sid), false);
		}
		catch (NotFoundException e)
		{
			isGranted = false;
		}
		return isGranted;
	}

	private static List<org.springframework.security.acls.model.Permission> expandPermissions(Permission permission)
	{
		if (permission == Permission.READ)
		{
			return Arrays
					.asList(BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE,
							BasePermission.ADMINISTRATION);
		}
		else if (permission ==Permission.WRITE)
		{
			return Arrays.asList(BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE,
					BasePermission.ADMINISTRATION);
		} else {
			// FIXME decide what to do here
			return Arrays
					.asList(BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE,
							BasePermission.ADMINISTRATION);
		}
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
